/**
 * 项目名称：mengyao
 * 创建日期：2018年6月14日
 * 修改历史：
 * 1、[2018年6月14日]创建文件 by zhaocs
 */
package cn.itcast.tag.web.user.service;

import cn.itcast.tag.web.user.bean.OrganizationBean;

import java.util.List;

/**
 * @author zhaocs
 *
 */
public interface OrganizationService {
    /**
     * 新增组织
     * @param bean
     * @return
     */
    Boolean addOrganization(OrganizationBean bean);

    /**
     * 根据ID删除组织
     * @param bean
     * @return
     */
    Boolean delOrganizationForId(OrganizationBean bean);

    /**
     *  根据ID修改组织
     * @param bean id、 name、flag、remark
     * @return
     */
    Boolean updateOrganization(OrganizationBean bean);

    /**
     * 根据组织id查询
     * @param bean id
     * @return
     */
    OrganizationBean queryForId(OrganizationBean bean);

    /**
     * 查询
     * @param bean
     * @return
     */
    List<OrganizationBean> queryForPid(OrganizationBean bean);

    /**
     * 查询
     * @param bean
     * @return
     */
    List<OrganizationBean> query(OrganizationBean bean);
}
