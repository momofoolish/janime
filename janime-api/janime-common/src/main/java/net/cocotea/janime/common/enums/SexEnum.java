package net.cocotea.janime.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author CoCoTea
 * @version 2.0.0
 */
@Getter
@AllArgsConstructor
public enum SexEnum {
    /**
     * 未知
     */
    UNKNOWN(0, "未知"),
    /**
     * 男
     */
    MAN(1, "男"),
    /**
     * 女
     */
    WOMAN(2, "女");

    final Integer code;
    final String desc;
}
