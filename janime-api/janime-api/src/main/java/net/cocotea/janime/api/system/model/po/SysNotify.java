/**
 *@Generated by sagacity-quickvo 5.0
 */
package net.cocotea.janime.api.system.model.po;

import java.io.Serializable;
import org.sagacity.sqltoy.config.annotation.Entity;
import org.sagacity.sqltoy.config.annotation.Id;
import org.sagacity.sqltoy.config.annotation.Column;
import lombok.Data;
import lombok.experimental.Accessors;
import java.math.BigInteger;
import java.sql.Timestamp;

/**
 * @project sqltoy-quickstart
 * @author CoCoTea
 * @version 2.0.0
 */
@Data
@Accessors(chain = true)
@Entity(tableName="sys_notify",comment="系统通知表",pk_constraint="PRIMARY")
public class SysNotify implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8014612710494442369L;
/*---begin-auto-generate-don't-update-this-area--*/	

	@Id(strategy="generator",generator="org.sagacity.sqltoy.plugins.id.impl.SnowflakeIdGenerator")
	@Column(name="id",comment="通知ID",length=19L,type=java.sql.Types.BIGINT,nativeType="BIGINT",nullable=false)
	private BigInteger id;

	@Column(name="title",comment="通知标题",length=200L,type=java.sql.Types.VARCHAR,nativeType="VARCHAR",nullable=false)
	private String title;

	@Column(name="memo",comment="消息内容",length=900L,type=java.sql.Types.VARCHAR,nativeType="VARCHAR",nullable=true)
	private String memo;

	/**
	 * 跳转链接
	 */
	@Column(name="jump_url",comment="跳转链接",length=200L,type=java.sql.Types.VARCHAR,nativeType="VARCHAR",nullable=true)
	private String jumpUrl;

	@Column(name="notify_type",comment="通知类型",length=20L,type=java.sql.Types.VARCHAR,nativeType="VARCHAR",nullable=false)
	private String notifyType;

	@Column(name="is_global",comment="是否全局",length=3L,defaultValue="0",type=java.sql.Types.TINYINT,nativeType="TINYINT",nullable=false)
	private Integer isGlobal;

	@Column(name="receiver",comment="接收人",length=19L,type=java.sql.Types.BIGINT,nativeType="BIGINT",nullable=true)
	private BigInteger receiver;

	@Column(name="level",comment="通知等级",length=3L,defaultValue="1",type=java.sql.Types.TINYINT,nativeType="TINYINT",nullable=false)
	private Integer level;

	@Column(name="notify_time",comment="通知时间",length=19L,type=java.sql.Types.TIMESTAMP,nativeType="TIMESTAMP",nullable=false)
	private Timestamp notifyTime;

	@Column(name="create_by",comment="创建人",length=19L,type=java.sql.Types.BIGINT,nativeType="BIGINT",nullable=false)
	private BigInteger createBy;

	@Column(name="create_time",comment="创建时间",length=19L,type=java.sql.Types.TIMESTAMP,nativeType="TIMESTAMP",nullable=false)
	private Timestamp createTime;

	@Column(name="is_deleted",comment="是否删除",length=3L,defaultValue="0",type=java.sql.Types.TINYINT,nativeType="TINYINT",nullable=false)
	private Integer isDeleted;
	/** default constructor */
	public SysNotify() {
	}
	
	/** pk constructor */
	public SysNotify(BigInteger id)
	{
		this.id=id;
	}
/*---end-auto-generate-don't-update-this-area--*/
}
