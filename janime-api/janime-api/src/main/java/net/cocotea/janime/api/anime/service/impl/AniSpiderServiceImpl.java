package net.cocotea.janime.api.anime.service.impl;

import cn.hutool.http.HttpUtil;
import com.sagframe.sagacity.sqltoy.plus.conditions.Wrappers;
import com.sagframe.sagacity.sqltoy.plus.conditions.query.LambdaQueryWrapper;
import com.sagframe.sagacity.sqltoy.plus.dao.SqlToyHelperDao;
import net.cocotea.janime.api.anime.model.po.AniOpus;
import net.cocotea.janime.api.anime.model.po.AniTag;
import net.cocotea.janime.api.anime.service.AniSpiderService;
import net.cocotea.janime.api.anime.service.AniTagService;
import net.cocotea.janime.common.constant.BgmDetailRuleConst;
import net.cocotea.janime.common.constant.CharConst;
import net.cocotea.janime.common.enums.IsEnum;
import net.cocotea.janime.common.enums.RssStatusEnum;
import net.cocotea.janime.common.model.BusinessException;
import net.cocotea.janime.properties.FileProp;
import org.seimicrawler.xpath.JXDocument;
import org.seimicrawler.xpath.JXNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class AniSpiderServiceImpl implements AniSpiderService {
    private final Logger logger = LoggerFactory.getLogger(AniSpiderServiceImpl.class);

    @Resource
    private FileProp fileProp;

    @Resource
    private SqlToyHelperDao sqlToyHelperDao;

    @Resource
    private AniTagService aniTagService;

    final String treaty = "https:";

    final String domain = "https://bgm.tv";

    @Transactional(rollbackFor = BusinessException.class)
    @Override
    public boolean addAniOpusByBgmUrl(String bgmUrl, Integer isCover) throws BusinessException {
        // 判断校验URL是否正确
        boolean startsWith = bgmUrl.startsWith("https://bgm.tv/subject/");
        if (!startsWith) {
            throw new BusinessException("错误的链接");
        }
        // 详细链接
        String[] split = bgmUrl.split(CharConst.LEFT_LINE);
        String detailUrl = CharConst.LEFT_LINE + split[split.length - 2] + CharConst.LEFT_LINE + split[split.length - 1];
        // 已存在的作品
        LambdaQueryWrapper<AniOpus> wrapper = Wrappers.lambdaWrapper(AniOpus.class)
                .eq(AniOpus::getDetailInfoUrl, detailUrl)
                .eq(AniOpus::getIsDeleted, IsEnum.N.getCode());
        AniOpus existOpus = sqlToyHelperDao.findOne(wrapper);
        // 解析HTML
        String html = HttpUtil.get(bgmUrl);
        List<AniTag> aniTagList = new ArrayList<>();
        AniOpus aniOpus = doParseHtmlToAcgOpus(html, aniTagList);
        aniOpus.setDetailInfoUrl(detailUrl);
        aniOpus.setRssStatus(RssStatusEnum.UNSUBSCRIBED.getCode());
        aniOpus.setRssLevelIndex(0);
        // 更新类型
        boolean isCoverFlag = (isCover != null && isCover == IsEnum.Y.getCode().intValue());
        logger.debug("isCoverFlag: {}", isCoverFlag);
        boolean updateFlag;
        if (isCoverFlag) {
            // 覆盖更新
            if (existOpus == null) {
                throw new BusinessException("作品不存在");
            }
            aniOpus.setId(existOpus.getId());
            Long update = sqlToyHelperDao.update(aniOpus);
            updateFlag = update > 0;
        } else {
            if (existOpus != null) {
                throw new BusinessException("作品已经存在");
            }
            // 新增才默认无资源
            aniOpus.setHasResource(IsEnum.N.getCode());
            // 新增保存
            Object save = sqlToyHelperDao.save(aniOpus);
            updateFlag = save != null;
        }
        // 标签关联
        aniTagService.saveTagOfOpus(aniTagList, aniOpus.getId());
        return updateFlag;
    }

    private AniOpus doParseHtmlToAcgOpus(String html, List<AniTag> aniTagList) throws BusinessException {
        JXDocument document = JXDocument.create(html);
        AniOpus aniOpus = new AniOpus();
        // 原名称
        JXNode nameOriginal = document.selNOne(BgmDetailRuleConst.NAME_ORIGINAL);
        if (nameOriginal != null) {
            aniOpus.setNameOriginal(nameOriginal.asString());
        }
        // 中文名称
        JXNode nameCn = document.selNOne(BgmDetailRuleConst.NAME_CN);
        if (nameCn != null) {
            aniOpus.setNameCn(nameCn.asString());
        }
        // 话数
        JXNode episodes = document.selNOne(BgmDetailRuleConst.EPISODES);
        if (episodes != null) {
            aniOpus.setEpisodes(episodes.asString());
        }
        // 放送开始
        JXNode launchStart = document.selNOne(BgmDetailRuleConst.LAUNCH_START);
        if (launchStart != null) {
            aniOpus.setLaunchStart(launchStart.asString());
        }
        // 放送星期
        JXNode deliveryWeek = document.selNOne(BgmDetailRuleConst.DELIVERY_WEEK);
        if (deliveryWeek != null) {
            aniOpus.setDeliveryWeek(deliveryWeek.asString());
        }
        // 简介
        List<JXNode> nodes = document.selN(BgmDetailRuleConst.ACG_SUMMARY);
        StringBuilder acgSummary = new StringBuilder();
        String wrap = "<br/>";
        for (int i = 0; i < nodes.size(); i++) {
            JXNode jxNode = nodes.get(i);
            JXNode one = jxNode.selOne(BgmDetailRuleConst.COMMON_TEXT);
            if (one != null) {
                acgSummary.append(one.asString());
                if (i < nodes.size() - 1) {
                    acgSummary.append(wrap);
                }
            }
        }
        aniOpus.setAniSummary(acgSummary.toString());
        // 抓取标签
        nodes = document.selN(BgmDetailRuleConst.ACG_TAGS);
        for (JXNode jxNode : nodes) {
            JXNode one = jxNode.selOne(BgmDetailRuleConst.ACG_TAG);
            if (one != null) {
                aniTagList.add(new AniTag().setTagName(one.asString()));
            }
        }
        // 抓取封面
        JXNode coverUrlNode = document.selNOne(BgmDetailRuleConst.COVER);
        if (coverUrlNode != null) {
            String coverUrl = coverUrlNode.asString();
            StringBuilder fullUrl = new StringBuilder();
            if (coverUrl.contains("lain.bgm.tv")) {
                fullUrl.append(treaty).append(coverUrl);
            } else {
                fullUrl.append(domain).append(coverUrl);
            }
            HttpUtil.downloadFile(fullUrl.toString(), fileProp.getAnimationCoverSavePath());
            // 获取封面的文件名
            String[] split = coverUrl.split("/");
            String fileName = split[split.length - 1];
            aniOpus.setCoverUrl(fileName);
        }
        return aniOpus;
    }

}
