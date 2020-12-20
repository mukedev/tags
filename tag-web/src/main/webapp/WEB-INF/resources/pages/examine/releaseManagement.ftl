<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <!--S logo-->
    <link rel="icon" href="${basePath}/res/imgs/favicon.ico" type="image/x-icon">
    <link rel="shortcut icon" href="${basePath}/res/imgs/favicon.ico" type="image/x-icon">
    <!--E logo-->
    <title>审核管理-标签管理系统</title>
    <meta name="keywords" content="大数据，用户标签管理系统">
    <meta name="description" content="">
    <meta name="viewport"
          content="width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=no"/>
    <!--优先使用 IE 最新版本和 Chrome -->
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1"/>
    <!--忽略数字自动识别为电话号码 -->
    <meta name="format-detection" content="telephone=no"/>
    <!--忽略识别邮箱 -->
    <meta name="format-detection" content="email=no"/>
    <!-- 针对手持设备优化，主要是针对一些老的不识别viewport的浏览器，比如黑莓 -->
    <meta name="HandheldFriendly" content="true"/>
    <link rel="stylesheet" type="text/css" href="${basePath}/res/lib/bootstrap3/css/bootstrap.css"/>
    <link rel="stylesheet" type="text/css" href="${basePath}/res/commons/css/base.css"/>
    <link rel="stylesheet" href="${basePath}/res/lib/jquery-confirm.min.css">
    <link rel="stylesheet" type="text/css" href="${basePath}/res/css/releaseManagement.css"/>
    <script src="${basePath}/res/lib/jquery1/jquery.min.js"></script>
    <script src="${basePath}/res/lib/jquery-confirm.min.js"></script>
</head>
<body>
<div class="container">
    <#include "./commons/header.ftl"/>
    <div class="centent clearfix">
        <div class="cont_left_label">
            <!--S 左侧的一级标签-->
            <div class="leftNav">
                <div class="con_gr">
                    <div class="left_gr">
                        <div class="firstGr" style="transition: 0.5s;position: relative;">
                            <img src="${basePath}/res/imgs/71.png">
                            <span value=${var1.id} title=${var1.name}><a href="${basePath}/examine/page/apply">申请管理</a></span>
                        </div>
                        <div class="firstGr" style="transition: 0.5s;position: relative;">
                            <img src="${basePath}/res/imgs/71.png">
                            <span value=${var1.id} title=${var1.name}><a
                                        href="${basePath}/examine/page/examine">审批管理</a></span>
                        </div>
                        <div class="firstGr" style="transition: 0.5s;position: relative;">
                            <img src="${basePath}/res/imgs/71.png">
                            <span value=${var1.id} title=${var1.name}><a
                                        href="${basePath}/examine/page/develop">开发管理</a></span>
                        </div>
                        <div class="firstGr" style="transition: 0.5s;position: relative;">
                            <img src="${basePath}/res/imgs/71.png">
                            <span value=${var1.id} title=${var1.name}><a
                                        href="${basePath}/examine/page/release">发布管理</a></span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <!--S cont_right_label-->
        <div class="cont_right_label">
            <!--S 导航-->
            <div class="label_nav">
                <img src="${basePath}/res/imgs/home.png" style="margin-left:12px;margin-top: -5px;"/>
                <span class="label_nav_sp2">您当前的位置：</span>
                <a href="javacript:void(0);" class="label_nav_sp1">首页</a>
                <span class="label_nav_sp2">></span>
                <a href="javacript:void(0);" class="label_nav_sp3">审核管理</a>
                <span class="label_nav_sp2">></span>
                <a href="javacript:void(0);" class="label_nav_sp3 active">发布管理</a>
            </div>
            <!--E 导航-->

            <!--S 据顶部的div-->
            <div class="labeltask_middle_wrap"></div>
            <!--E 据顶部的div-->
            <!--S 图表的内容-->
            <div class="labeltask_bottom_wrap">
                <section class="label_bottom_wrap">
                    <div class="User_center">
                        <div id="User_centerIN" class="User_centerIN">
                            <div class="User_centerIN_top">
                                <div class="User_radio_sosuo">
                                    <input class="User_input" type="text" name="" id="query" value=""
                                           placeholder="请输入关键词检索"/>
                                    <span class="Userl_radio6"></span>
                                    <span class="shenpi">批量审批</span>
                                </div>
                            </div>
                            <div class="User_centerIN_tubiao current_table">
                                <!--table-->
                                <section class="table_wrap clearfix all-table-container">
                                    <div class="cover_style">
                                        <!--用户列表失败-->
                                        <div>
                                            <table id="failtable"
                                                   class="table table-striped table-bordered table-hover table-text-center">
                                                <thead class="all-table-header">
                                                <tr>
                                                    <th width="" style="text-align: left;"><input type="checkbox">全选
                                                    </th>
                                                    <th width="">申请类型</th>
                                                    <th width="">申请要求</th>
                                                    <th width="">标签名称</th>
                                                    <th width="">标签规则</th>
                                                    <th width="">算法模型</th>
                                                    <th width="">申请人</th>
                                                    <th width="">最后处理人</th>
                                                    <th width="">处理结果</th>
                                                    <th width="">审批理由</th>
                                                    <th width="">审批状态</th>
                                                    <th width="">操作</th>
                                                </tr>
                                                </thead>
                                                <tbody>
                                                <tr>
                                                    <td style="text-align: left;"><input type="checkbox">1</td>
                                                    <td>组合标签</td>
                                                    <td>组合标签创建</td>
                                                    <td>A小A</td>
                                                    <td title="123123123123" style="cursor: pointer;">
                                                        <div style="    width: 66px;overflow: hidden;white-space: nowrap;text-overflow: ellipsis;">
                                                            123123123123
                                                        </div>
                                                    </td>
                                                    <td title="模型1模型1模型1模型1" style="cursor: pointer;">
                                                        <div style="width: 66px;overflow: hidden;white-space: nowrap;text-overflow: ellipsis;">
                                                            模型1模型1模型1模型1
                                                        </div>
                                                    </td>
                                                    <td>demo</td>
                                                    <td>--</td>
                                                    <td></td>
                                                    <td>2016-01-10 18:10:10</td>
                                                    <td>
                                                        <div class="work workFail">申请中</div>
                                                    </td>
                                                    <td class="resale" data-id="1">发布审批</td>
                                                </tr>
                                                <tr>
                                                    <td style="text-align: left;"><input type="checkbox">2</td>
                                                    <td>组合标签</td>
                                                    <td>组合标签创建</td>
                                                    <td>A小A</td>
                                                    <td title="123123123123" style="cursor: pointer;">
                                                        <div style="    width: 66px;overflow: hidden;white-space: nowrap;text-overflow: ellipsis;">
                                                            123123123123
                                                        </div>
                                                    </td>
                                                    <td title="模型1模型1模型1模型1" style="cursor: pointer;">
                                                        <div style="width: 66px;overflow: hidden;white-space: nowrap;text-overflow: ellipsis;">
                                                            模型1模型1模型1模型1
                                                        </div>
                                                    </td>
                                                    <td>demo</td>
                                                    <td>--</td>
                                                    <td></td>
                                                    <td>2016-01-10 18:10:10</td>
                                                    <td>
                                                        <div class="work workFail">开发完成</div>
                                                    </td>
                                                    <td class="resale" data-id="1">发布审批</td>
                                                </tr>
                                                </tbody>
                                            </table>
                                        </div>
                                    </div>
                                </section>
                                <!--/.table-->
                                <!--pages-->
                                <!--number-->
                                <!--  <section class="pages">
                                    <div class="col-sm-6">
                                        <div class="dataTables_info page-num"  role="status" aria-live="polite">
                                            当前第<span id='curr'>1</span>/<span id='tota'>50</span>页，每页<span id='size'>10</span>条，共<span id='reco'>1</span>条记录
                                        </div>
                                    </div>
                                    上一頁，下一頁
                                    <div class="col-sm-6">
                                        <div class="dataTables_info page-num page_num_right" id="fail-table_info" role="status" aria-live="polite">
                                            <span class="home_page" id='home_page'>首页</span>
                                            <span class="up_page" id='up_page'>上一页</span>
                                            <span class="next_page" id='next_page'>下一页</span>
                                            <input class="search_page_num" onkeyup="this.value=this.value.replace(/[^\d]/g,'');"></input>
                                            <span class="home_page" id='end_page'>尾页</span>
                                            <span class="search_page" id='search_page'>跳页</span>
                                        </div>
                                    </div>
                                </section> -->
                            </div>
                        </div>
                    </div>
                </section>

            </div>
            <!--E 图表的内容-->
        </div>
        <!--E cont_right_label-->
    </div>
    <!-- 模态框（Modal） -->
    <!-- <section >
        <div class="modal fade" id="popermodal" tabindex="-1" role="dialog"
             aria-labelledby="popermodalLabel" aria-hidden="true">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">
                            ×
                        </button>
                        <h4 class="modal-title" id="popermodalLabel">
                               上传算法
                        </h4>
                    </div>
                    一级标签
                    <div class="modal-body modal-body_wrap">
                        <span class="typename_span">一级标签：</span>
                        <input type="text" id ="levelone" class="form-control typename_input" value="" readonly>
                    </div>
    
                    二级标签
                    <div class="modal-body modal-body_wrap">
                        <span class="typename_span">二级标签：</span>
                        <input type="text" id = "leveltwo" class="form-control typename_input"value="" readonly>
                    </div>
                    三级标签
                    <div class="modal-body modal-body_wrap">
                        <span class="typename_span">三级标签：</span>
                        <input type="text" id = "levelthree" class="form-control typename_input" value="" readonly>
                    </div>
                    四级标签
                    <div class="modal-body modal-body_wrap">
                        <span class="typename_span">四级标签：</span>
                        <input type="text" id = "levelfour" class="form-control typename_input" value="" readonly>
                    </div>
                    更新周期
                    <div class="modal-body modal-body_wrap">
                        <span class="typename_span">更新周期：</span>
                        <input type="text" class="form-control typename_input" value="每月" readonly>
                    </div>
    
                    业务含义文本框
                    <div class="modal-body modal-body_wrap type_wrap">
                        <span class="typename_datatype">业务含义：&nbsp;</span>
                        <textarea readonly type="text" maxlength="50" class="form-control  form-input  business_mean"
                                  placehodler="请描述该标签在业务山过的作用" id ="meanings"></textarea>
    
                    </div>
    
                    标签规则文本框
                    <div class="modal-body modal-body_wrap type_wrap">
                        <span class="typename_datatype">标签规则：&nbsp;</span>
                        <textarea readonly type="text" maxlength="50" class="form-control  form-input  business_mean"
                                  placehodler="请描述该标签产出规则" id ="tagRules"></textarea>
    
                    </div>
                    上传算法
                    <div class="modal-body modal-body_wrap type_wrap">
                        <span class="typename_datatype">上传算法：&nbsp;</span>
                        <form id="fileupload" action="" method="post" style="width:72%">
                              <input id="file-Portrait" type="file" class="file" data-show-preview="false">
                        </form>
                    </div>
                    <div class="instruction_upload">*上传算法包需包含完整输入、输出形式，算法包规则，数据源接入形式以及更新周期规则</div>
                    <div class="modal-footer" style="text-align:center;">
                        <button id="createchildrentype" type="button" class="btn btn-primary execute">
                            执行
                        </button>
                        <button type="button" class="btn btn-default" data-dismiss="modal">
                            取消
                        </button>
                    </div>
                </div>/.modal-content
            </div>/.modal
        </div>
    </section> -->
    <!--S 群体模态框（Modal） -->
    <!-- <section >
        <div class="modal fade" id="userpopermodal" tabindex="-1" role="dialog"
             aria-labelledby="userpopermodalLabel" aria-hidden="true">
            <div class="modal-dialog">
                <div class="modal-content">
                    <form id="userfileupload" action="" method="post" style="width:72%">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">
                            ×
                        </button>
                        <h4 class="modal-title" id="userpopermodalLabel">
                            上传算法
                        </h4>
                    </div>
                    一级标签
                    <div class="modal-body modal-body_wrap">
                        <span class="typename_span">一级标签：</span>
                        <input type="text" id = "userlevelone" class="form-control typename_input" value="" readonly>
                    </div>
    
                    二级标签
                    <div class="modal-body modal-body_wrap">
                        <span class="typename_span">二级标签：</span>
                        <input type="text" id = "userleveltwo" class="form-control typename_input"value="" readonly>
                    </div>
    
                    定义条件
                    <div class="modal-body modal-body_wrap type_wrap">
                        <span class="typename_datatype">定义条件：&nbsp;</span>
                        <textarea readonly type="text" id = "definiteconditions" maxlength="50" class="form-control  form-input  business_mean"
                                  placehodler="定义条件"></textarea>
                    </div>
    
                    群体含义文本框
                    <div class="modal-body modal-body_wrap type_wrap">
                        <span class="typename_datatype">群体含义：&nbsp;</span>
                        <textarea readonly type="text" maxlength="50" id = "usermeanings" class="form-control  form-input  business_mean"
                                  placehodler="请描述该标签在业务山过的作用"></textarea>
                    </div>
    
                    群体用途文本框
                    <div class="modal-body modal-body_wrap type_wrap">
                        <span class="typename_datatype">群体用途：&nbsp;</span>
                        <textarea readonly type="text" maxlength="50" id = "userpurpose" class="form-control  form-input  business_mean"
                                  placehodler="请描述该标签产出规则"></textarea>
                    </div>
    
                    有效时间
                    <div class="modal-body modal-body_wrap">
                        <span class="typename_span">有效时间：</span>
                        <input type="text" class="form-control typename_input" id="validtime"  value="" readonly>
                    </div>
    
                    上传算法
                    <div class="modal-body modal-body_wrap type_wrap">
    
                        <span class="typename_datatype">上传文件：&nbsp;</span>
                        
                            <input id="userfile-Portrait" type="file" class="file" data-show-preview="false">
                        
                    </div>
                    <div class="instruction_upload">*上传算法包需包含完整输入、输出形式，算法包规则，数据源接入形式以及更新周期规则</div>
                    <div class="modal-footer" style="text-align:center;">
                        <button type="button" class="btn btn-default" data-dismiss="modal">
                            取消
                        </button>
                        <button  type="button" class="btn btn-primary execute" >
                            执行
                        </button>
                    </div>
                    </form>
                </div>
                /.modal-content
            </div>
            /.modal
        </div>
    </section> -->
    <!--E 群体模态框（Modal） -->

    <!--E 中间的内容-->
    <script type="text/javascript" src="${basePath}/res/lib/bootstrap3/js/bootstrap.js"></script>
    <script type="text/javascript" src="${basePath}/res/commons/js/base.js"></script>
    <script type="text/javascript" src="${basePath}/res/lib/layer/layer.js"></script>
    <script type="text/javascript" src="${basePath}/res/js/examine/releaseManagement.js"></script>
</body>
</html>

