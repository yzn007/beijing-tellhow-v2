/**
 * 添加公共指标
 *
 * @class AddWindow
 * @extends Ext.Window
 */
Ext.form.Field.prototype.msgTarget='under';
AddWindow = Ext.extend(Ext.Window, {
	title : '添加公共指标',
	width : 460,
	height : 450,
	layout : 'fit',
	plain : true,
	modal : true,
	bodyStyle : 'padding:10px;',
	buttonAlign : 'center',
	id : 'addWindow',
	listeners : {
		close : function() {
			Ext.getCmp("addWindow").destroy();
		},
        afterRender: function() {
            var combo = Ext.getCmp("sourceTypeId");
            var cmbType = Ext.getCmp("objTypeId");
            //预警指标显示
            if (combo.getValue()!='04'){
                cmbType.hide();
            }
        }
	},
	initComponent : function() {
		var comp = null;

		if (selectNodeId == 'root' || selectNodeId == '') {
			comp = new ObjCateSelector();
		} else {
			comp = new Ext.Panel({
				layout : 'form',
				border : false,
				split : false,
				anchor : '100%',
				bodyStyle : 'padding : 0px',
				baseCls : 'x-plain',
				labelWidth : 80,
				labelAlign : 'left',
				items : [{
					xtype : 'hidden',
					name : 'obj_cate_id',
					id : 'obj_cate_id_add',
					value : selectNode.attributes.obj_cate_id
				}, {
					xtype : 'textfield',
					name : 'obj_cate_desc',
					fieldLabel : '考核对象类型',
					readOnly : true,
					value : selectNode.attributes.obj_cate_id == 'BM'
							? '区属部门'
							: (selectNode.attributes.obj_cate_id == 'CBM'? '市属部门': '镇街'),
					anchor : '95%'
				}]
			})
		}

		Ext.applyIf(this, {
			items : [{
				xtype : 'form',
				id : 'addForm',
				baseCls : 'x-plain',
				border : false,
				labelWidth : 100,
				labelAlign : 'left',
				layout : 'form',
                timeout : 600000,
				url : pathUrl
						+ '/publicMeasure_common.action?method=addEngMeasure',
				items : [{
					xtype : 'textfield',
					fieldLabel : '指标ID<span style="color:red;font-weight:bold" data-qtip="Required">*</span>',
					allowBlank : false,
					id : 'measure_id',
					name : 'measure_id',
					anchor : '95%',
					listeners : {
						blur : function(field){
							if(field.validate()){
								Ext.Ajax.request({
									url : pathUrl + '/publicMeasure_checkMeasure.action',
									params : {measure_id : field.getValue()},
									method : 'POST',
									callback : function(options,request,response){
										var json = Ext.util.JSON.decode(response.responseText);
										if(json.success){
											Ext.getCmp('save_btn').setDisabled(false);
										}else{
											field.markInvalid(json.info);
											Ext.getCmp('save_btn').setDisabled(true);
										}
									}
								});
							}
						}
					}
				}, {
					xtype : 'textfield',
					fieldLabel : '指标名称',
					allowBlank : false,
					id : 'measure_name',
					name : 'measure_name',
					anchor : '95%'
				},{
                    xtype : 'textfield',
                    fieldLabel : '指标单位',
                    allowBlank : true,
                    id : 'measure_unit',
                    name : 'measure_unit',
                    anchor : '95%'
                },
					new ObjectSourceFromSelector(),
					new SourceTypeSelector(),
					new ObjAlertTypeSelector(),
					/*comp,*/
					new ObjectCountPeriod(),
                    {
                        xtype : 'combo',
                        store : dimensionOtherStore,
                        valueField : 'link_id',
                        displayField : 'link_name',
                        mode : 'local',
                        forceSelection : true,
                        hiddenName : 'obj_link_id',
                        editable : false,
                        triggerAction : 'all',
                        allowBlank : true,
                        emptyText : '请选择维度',
                        fieldLabel : '其它维度',
                        listeners: {
                            select : function(combo, record, index){
                                // beforeMeaClose(this.id);
                                // objDimDS.reload({params : {
                                //     link_id : record.get('link_id')
                                // }})
								// var otherObject = Ext.getCmp('other_object_table');
                                // otherObject.setValue(record.get('link_id'));

                                Ext.getCmp('objDimSet_desc').setValue(record.data.link_name);
                                Ext.getCmp('other_object_table').setValue(record.data.link_id);
                            }
                        },
                        name : 'obj_link_id',
                        id : 'isDimension',			//对象维度
                        anchor : '95%'
                    },
                    {
                        id : 'objDimSet',
                        columnWidth : .35,
                        anchor : '95%',
                        layout : 'form'
                    },
                    {
                        xtype : 'combo',
                        store : districtDimensionStore,
                        valueField : 'link_id',
                        displayField : 'link_name',
                        mode : 'local',
                        forceSelection : true,
                        hiddenName : 'obj_district_id',
                        editable : false,
                        triggerAction : 'all',
                        allowBlank : false,
                        fieldLabel : '地区维度<span style="color:red;font-weight:bold" data-qtip="Required">*</span>',
                        listeners: {
                            select : function(combo, record, index){
                                // beforeMeaClose(this.id);
                                // objDistrictDim.reload({params : {
                                //     link_id : record.get('link_id')
                                // }});
                                // var districtObject = Ext.getCmp('district_object_table');
                                // districtObject.setValue(record.get('link_id'));
                                console.info(record);
                                Ext.getCmp('objDistrictDimSet_desc').setValue(record.data.link_name);
                                Ext.getCmp('district_object_table').setValue(record.data.link_id);
                            }
                        },
                        name : 'obj_district_id',
                        id : 'districtDimension',			//地区维度
                        anchor : '95%'
                    },
                    {
                        id : 'objDistrictDimSet',
                        columnWidth : .35,
                        anchor : '95%',
                        layout : 'form'
                    },
					{
					xtype : 'numberfield',
					name : 'inner_level_order',
					fieldLabel : '同级顺序',
					allowBlank : true,
					anchor : '95%'
				}, {
					xtype : 'textarea',
					fieldLabel : '指标描述',
					id : 'measure_desc',
					name : 'measure_desc',
					height : 60,
					anchor : '95%'
				}, {
					xtype : 'hidden',
					id : 'parent_measure_id',
					name : 'parent_measure_id',
					anchor : '95%',
					value : selectNodeId == '' ? 'root' : selectNodeId
				}, {
                        xtype : 'hidden',
                        id : 'measure_source_desc'
                    }
                    ,{
                        xtype : 'hidden',
                        id : 'objDistrictDimSet_desc'
                    }
                    , {
                        xtype : 'hidden',
                        id : 'objDimSet_desc'
                    }
                    , {
                        xtype : 'hidden',
                        id : 'district_object_table'
                    }
                    , {
                        xtype : 'hidden',
                        id : 'other_object_table'
                    }
				]
			}],
			buttons : [{
				text : '保存',
				id : 'save_btn',
				handler : function() {
					var formPanel = Ext.getCmp("addForm");
					var nodeID = Ext.getCmp("measure_id").getValue();
					var nodeName = Ext.getCmp("measure_name").getValue();
					if (formPanel.form.isValid()) {
						formPanel.form.submit({
							waitMsg : '正在处理,请稍候......',
							failure : function(form, action) {
								Ext.MessageBox.alert('错误', action.result.info);
							},
							success : function(form, action) {
								var objCateId = '';
								if (selectNodeId == 'root'
										|| selectNodeId == '') {
									objCateId = Ext.getCmp("objCateId")
											.getValue();
								} else
									objCateId = Ext.getCmp("obj_cate_id")
											.getValue();
								var node = new Ext.tree.TreeNode({
									id : nodeID,
									text : createAddOrder()+'[' + nodeID + ']' + nodeName,
									source_type_id : Ext.getCmp("sourceTypeId")
											.getValue(),
									obj_cate_id : objCateId,
									leaf : false
								});
								engMeasureTree.getSelectionModel()
										.getSelectedNode().appendChild(node);
								engMeasureTree.getSelectionModel()
										.getSelectedNode().expand();
								Ext.getCmp("addWindow").destroy();
							}
						});
                        //调用添加指标存储过程
                        Ext.Ajax.request({
                            url : pathUrl + '/publicMeasure_common.action?method=engMeasureExeProc',
                            method : 'POST'
                        });
					} else {
						Ext.MessageBox.alert('错误', '请填写必输项！');
					}
				}
			}, {
				text : '取消',
				handler : function() {
					Ext.getCmp("addWindow").destroy();
				}
			}]
		})
		AddWindow.superclass.initComponent.call(this);
	}
});

function initCompons() {
    //其它对象维度
    dimensionOtherStore.on("load", function() {
        //删除统计年份维度
        for(var i=dimensionOtherStore.getCount()-1;i>=0;i--){
            if(dimensionOtherStore.data.items[i].data.link_name.indexOf('地区代码')>=0
                || dimensionOtherStore.data.items[i].data.link_name.indexOf('统计年份')>=0
            ){
                dimensionOtherStore.data.items[i].store.removeAt(i);
            }
        }

        // if (dimensionOtherStore.getCount() > 0) {
        //
        //     var  dimensionId = dimensionOtherStore.getAt(0).get('link_id');
        //
        //     Ext.getCmp("isDimension").setValue(dimensionId);
        // }


        if (Ext.getCmp('isDimension') != null)
            Ext.getCmp('isDimension').setValue("");

    });
    dimensionOtherStore.load();

    //地区维度
    districtDimensionStore.on('load',function(){
        //删除其它维度
        for(var i=districtDimensionStore.getCount()-1;i>=0;i--){
            if(districtDimensionStore.data.items[i].data.link_name.indexOf('地区代码') < 0 ){
                districtDimensionStore.data.items[i].store.removeAt(i);
            }
        }
        if (districtDimensionStore.getCount() > 0) {

            var  dimensionId = districtDimensionStore.getAt(0).get('link_id');

            if (Ext.getCmp("districtDimension") != null)
                Ext.getCmp("districtDimension").setValue(dimensionId);
        }
    });
    districtDimensionStore.load();

    //分类目录
    var combo = Ext.getCmp("sourceTypeId");
    var uid = combo.getValue();
    combo.fireEvent("select",combo,combo.getStore().getById(uid));

    //指标来源
    var combo = Ext.getCmp("objSourceId");
    var uid = combo.getValue();
    combo.fireEvent("select",combo,combo.getStore().getById(uid));
}

function doAddLoad(){
    var addwindow = new AddWindow();
    // Ext.getCmp("addForm").form.load({
    //   success: function (store, op, options) {
    //
    //    },
    //    failure: function (form, action) {
    //        // var combo = Ext.getCmp("sourceTypeId");
    //        // var uid = combo.getValue();
    //        // combo.fireEvent('select', combo, combo.getStore().getById(uid));
    //    }
    // });
    addwindow.show();
    initCompons();
}
var compSource = null;
/**
 * 编辑公共指标
 */
function doEdit() {
    compSource = new ObjectSourceFromSelector();
	var editWindow = new EditWindow();
	Ext.getCmp("pmid").setValue(selectNode.parentNode.text);
	Ext.getCmp("tpmid").setValue(selectNode.parentNode.id);
	Ext.getCmp("tmid").setValue(selectNode.id);
	Ext.getCmp("editForm").form.load({
		url : pathUrl + '/publicMeasure_common.action?method=getEngMeasureById',
		params : {
			measure_id : selectNode.id
		},
        success:function(form ,action){
            console.info(action.result.data);
            //指标来源
			var record = eval(action.result.data);
			var comb = Ext.getCmp("objSourceId");
			comb.setValue(record.measure_source);


            Ext.getCmp('sourceTypeId').setValue(record.source_type_id);
            Ext.getCmp('sourceTypeId').disabled = true;

            Ext.getCmp('measure_unit').show();
            Ext.getCmp('objSourceId').show();


            Ext.getCmp('obj_district_id').setValue(record.districtdimension_desc);
            Ext.getCmp('obj_district_id').disabled = true;

            Ext.getCmp('obj_link_id').setValue(record.ohterdimension_desc);
            Ext.getCmp('obj_link_id').disabled = true;

            Ext.getCmp('inner_level_order').setValue(record.inner_level_order);

            switch (record.countperiod){
                case "00":
                    val = "月";
                    break;
                case "01":
                    val = "季";
                    break;
                case "02":
                    val = "年";
                    break;
                case "03":
                    val = "日";
                    break;
                default:
                    val = "";
                    break;
            }
            Ext.getCmp('countperiod_txt').setValue(val);

        }
	});

	editWindow.show();
    initCompons();
}
/**
 * 编辑公共指标
 */
EditWindow = Ext.extend(Ext.Window, {
	title : '编辑公共指标',
	width : 500,
	height : 400,
	layout : 'fit',
	plain : true,
	modal : true,
	bodyStyle : 'padding:10px;',
	buttonAlign : 'center',
	id : 'editWindow',
	listeners : {
		close : function() {
			Ext.getCmp("editWindow").destroy();
		}
	},
	initComponent : function() {


		Ext.applyIf(this, {
			items : [{
				xtype : 'form',
				// region : 'center',
				id : 'editForm',
				// width : 270,
				bodyStyle : 'padding:10px;',
				border : false,
				labelWidth : 80,
				labelAlign : 'left',
				layout : 'form',
				url : pathUrl
						+ '/publicMeasure_common.action?method=editEngMeasure',
                timeout : 600000,
				reader : new Ext.data.JsonReader({
					root : 'results'
				}, [{name : 'measure_id'},
                    {name : 'source_type_id'},
                    {name : 'measure_name'},
                    {name : 'source_type_desc'},
                    {name : 'obj_cate_desc'},
                    {name : 'formula_expr'},
                    {name : 'formula_desc'},
                    {name : 'measure_desc'},
                    {name : 'obj_cate_id'},
                    {name : 'measure_source'},
                    {name : 'measure_unit'},
                    {name : 'countperiod'},
                    {name : 'districtdimension'},
                    {name : 'ohterdimension'},
                    {name : 'countperiod_desc'},
                    {name : 'districtdimension_desc'},
                    {name : 'ohterdimension_desc'},
                    {name : 'alerttype'},
                    {name : 'districtdimension_desc'},
                    {name : 'ohterdimension_desc'},
                    {name : 'inner_level_order'}
                    ]
                ),
				items : [{
                    xtype: 'hidden',
                    fieldLabel: '上级节点',
                    id: 'pmid',
                    name: 'pMeasureID',
                    readOnly: true,
                    anchor: '95%'
                },
                    {
                        xtype : 'textfield',
                        fieldLabel : '指标ID',
                        allowBlank : false,
                        id : 'tmid',
                        disabled : true,
                        name : 'new_measure_id',
                        anchor : '95%'
                    }, {
                        xtype : 'textfield',
                        fieldLabel : '指标名称',
                        allowBlank : false,
                        id : 'measure_name',
                        name : 'measure_name',
                        anchor : '95%'
                    },{
                        xtype : 'textfield',
                        fieldLabel : '指标单位',
                        allowBlank : true,
                        id : 'measure_unit',
                        name : 'measure_unit',
                        anchor : '95%'
                    },
                    new ObjectSourceFromSelector(),
                    new SourceTypeSelector(),
                    new ObjAlertTypeSelector(),
                    /*comp,*/
                    {
                        xtype : 'textfield',
                        fieldLabel : '统计周期',
                        allowBlank : true,
                        name: 'countperiod_txt',
                        id: 'countperiod_txt',
                        disabled: true,
                        anchor : '95%'
                    },
                    {
                        xtype : 'textfield',
                        fieldLabel : '其它维度',
                        allowBlank : false,
                        id : 'obj_link_id',
                        disabled : true,
                        anchor : '95%'
                    },
                    {
                        id : 'objDimSet',
                        columnWidth : .35,
                        anchor : '95%',
                        layout : 'form'
                    },
                    {
                        xtype : 'textfield',
                        fieldLabel : '地区维度',
                        allowBlank : false,
                        id : 'obj_district_id',
                        disabled : true,
                        anchor : '95%'
                    },
                    {
                        id : 'objDistrictDimSet',
                        columnWidth : .35,
                        anchor : '95%',
                        layout : 'form'
                    },
                    {
                        xtype : 'numberfield',
                        id : 'inner_level_order',
                        name : 'inner_level_order',
                        fieldLabel : '同级顺序',
                        allowBlank : true,
                        anchor : '95%'
                    }, {
                        xtype : 'textarea',
                        fieldLabel : '指标描述',
                        id : 'measure_desc',
                        name : 'measure_desc',
                        height : 60,
                        anchor : '95%'
                    }, {
                        xtype : 'hidden',
                        id : 'tpmid',
                        name : 'parent_measure_id',
                        anchor : '95%',
                        value : selectNodeId == '' ? 'root' : selectNodeId
                    },
                    {
                        xtype : 'hidden',
                        id : 'oldMeasureID',
                        name : 'measure_id'
                    },
                    {
                        xtype : 'hidden',
                        id : 'measure_source_desc'
                    }
                    ,{
                        xtype : 'hidden',
                        id : 'objDistrictDimSet_desc'
                    }
                    , {
                        xtype : 'hidden',
                        id : 'objDimSet_desc'
                    }
                    , {
                        xtype : 'hidden',
                        id : 'district_object_table'
                    }
                    , {
                        xtype : 'hidden',
                        id : 'other_object_table'
                    }

                ]
			}/**
				 * , { xtype : 'treepanel', id : 'mTree', region : 'center',
				 * frame : false, title : '变更上级节点', loader : new
				 * Ext.tree.TreeLoader(), lines : false, listeners : { click :
				 * function(n,e){ Ext.getCmp('tpmid').setValue(n.id); } },
				 * bodyStyle : 'padding:5px 5px', autoScroll : true, root :
				 * getRootNode('root', '私有指标树', expandEngMeasureTreeNode) }
				 */
			],
			buttons : [{
				text : '保存',
				handler : function() {
					var formPanel = Ext.getCmp("editForm");
					if (formPanel.form.isValid()) {
						var npid = Ext.getCmp('tpmid').getValue();
						if (false && npid != '' && npid != selectNode.id
								&& npid != selectNode.parentNode.id) {
							Ext.Msg.show({
								title : '提示信息',
								msg : '是否变更指标上级节点为:['
										+ Ext.getCmp('tpmid').getValue() + ']',
								buttons : {
									yes : '确定',
									no : '不变更',
									cancel : '取消'
								},
								icon : Ext.MessageBox.QUESTION,
								fn : function(bid) {
									if (bid == 'yes') {
										formPanel.form.submit({
											waitMsg : '正在处理，请稍候......',
											failure : function(form, action) {
												Ext.MessageBox.alert('错误',
														action.result.info);
											},
											success : function(form, action) {
												engMeasureTree.getRootNode()
														.reload();
												Ext.getCmp("editWindow")
														.destroy();
												propertyPanel.form.reset();
												var selectNodeId = '';
												var selectNode = null;
											}
										});
									} else if (bid == 'no') {
										Ext
												.getCmp('tpmid')
												.setValue(selectNode.parentNode.id);
										formPanel.form.submit({
											waitMsg : '正在处理，请稍候......',
											failure : function(form, action) {
												Ext.MessageBox.alert('错误',
														action.result.info);
											},
											success : function(form, action) {
												var nodeID = Ext
														.getCmp("oldMeasureID")
														.getValue();
												var nodeName = Ext
														.getCmp("measure_name")
														.getValue();
												engMeasureTree
														.getSelectionModel()
														.getSelectedNode()
														.setText('[' + nodeID
																+ ']'
																+ nodeName);
												propertyPanel.form.load({
													url : pathUrl
															+ '/publicMeasure_common.action?method=getEngMeasureById&measure_id='
															+ nodeID
												});
												Ext.getCmp("editWindow")
														.destroy();
											}
										});
									}
								}
							})
						} else {
							Ext.getCmp('tpmid')
									.setValue(selectNode.parentNode.id);
							formPanel.form.submit({
								params : {
									new_measure_id : selectNode.id
								},
								waitMsg : '正在处理，请稍候......',
								failure : function(form, action) {
									Ext.MessageBox.alert('错误',
											action.result.info);
								},
								success : function(form, action) {
									var nodeID = Ext.getCmp("tmid").getValue();
									var nodeName = Ext.getCmp("measure_name")
											.getValue();
									engMeasureTree.getSelectionModel()
											.getSelectedNode().id = nodeID;

									engMeasureTree.getSelectionModel()
											.getSelectedNode().setText('['
													+ nodeID + ']' + nodeName);
									propertyPanel.form.load({
										url : pathUrl
												+ '/publicMeasure_common.action?method=getEngMeasureById&measure_id='
												+ nodeID
									});
									Ext.getCmp("editWindow").destroy();
                                    //调用编辑指标存储过程
                                    Ext.Ajax.request({
                                        url : pathUrl + '/publicMeasure_common.action?method=editEngMeasureExeProc',
                                        method : 'POST'
                                    });
								}
							});

						}

					} else {
						Ext.MessageBox.alert('错误', '请填写必输项!');
					}
				}
			}, {
				text : '取消',
				handler : function() {
					Ext.getCmp("editWindow").destroy();
				}
			}]
		})
		EditWindow.superclass.initComponent.call(this);
	}
});

/**
 * 导入指标
 */
function doImport(){
    var importWindow = new ImportWindow();
    importWindow.show();
}

ImportWindow = Ext.extend(Ext.Window, {
    title : '导入指标窗口',
    width : 330,
    height : 200,
    layout : 'fit',
    plain : true,
    modal : true,
    bodyStyle : 'padding:10px;',
    buttonAlign : 'center',
    id : 'importWindow',
    listeners : {
        close : function() {
            Ext.getCmp("importWindow").destroy();
        }
    },
    initComponent : function() {

        Ext.applyIf(this, {
            items : [{
                xtype : 'form',
                region : 'center',
                id : 'importForm',
                width : 300,
                bodyStyle : 'padding:10px;10px;55px;10px',
                border : false,
                labelWidth : 80,
                labelAlign : 'left',
                layout : 'form',
                url : pathUrl + '/bscresult_importMeasureFromExcel.action',
                timeout : 60000,
                fileUpload: true,
                items : [
                    {
                        id : 'template_id',
                        fieldLabel : '模板下载',
                        anchor : '95%',
                        // title: '模板下载',
                        width: '80%',
                        html: '<a href="'+pathUrl + '/bscresult_importBscMeasureTemplateDownload.action" >'+'下载导入模板</a>'
                    },
                    {
                        id:'file',
                        anchor : '95%',
                        xtype : 'textfield',
                        allowBlank :false,
                        // width:"90%",
                        blankText:"请选择文件",
                        inputType:'file'//文件组件
                    }
                ],
                buttons:[{
                    text : '提交',
                    handler : function() {
                        var formPanel = Ext.getCmp('importForm');
                        if(formPanel.form.isValid()){
                            var file=formPanel.form.findField("file");
                            var str = file.getValue();
                            var filename=str;
                            str = str.substr(str.indexOf('.'),str.length - 1);
                            if("" == str || str==".exe") {
                                Ext.MessageBox.alert("提示","文件不能为空或者以.exe结尾！");
                                return;
                            }
                            // var options = {
                            //     type:'POST',
                            //     success:showResponse,
                            //     dataType: 'json',
                            //     error : function(xhr, status, err) {
                            //         Ext.Msg.alert('失败', '指标导入失败！');
                            //         Ext.getCmp('importWindow').destroy();
                            //     }
                            // };
                            // formPanel.form.submit(function () {
                            //     $(this).ajaxSubmit(options);
                            //     return false;
                            // });
                            formPanel.form.submit({
                                // type:'ajax',
                                params:{fileName:filename},
                                method : 'POST',
                                waitMsg: '正在提交数据...',
                                success: function(form, action){
                                    if(action.result.unImport!=""){
                                        Ext.Msg.alert('信息',action.result.unImport);
                                    }else
                                        Ext.Msg.alert('成功','指标导入完成！');
                                    Ext.getCmp('importWindow').destroy();
                                },
                                failure: function(form, action){
                                    Ext.Msg.alert('失败', '指标导入失败！');
                                    Ext.getCmp('importWindow').destroy();
                                }
                            });



                        }else{
                            Ext.MessageBox.alert("提示信息",'请选择导入的数据文件！');
                        }

                    }
                },{
                    text : '取消',
                    handler : function() {
                        Ext.getCmp('importWindow').destroy();
                    }
                }]
            }]}
        )
        ImportWindow.superclass.initComponent.call(this);
    }
});

function showResponse(responseText, statusText, xhr, $form){
    if(responseText.status == "0"){
        alert(responseText.msg);
    }
}

    /**
 * 高级查询
 * @param n
 */
function doSearch(){
    compSource = new ObjectSourceFromSelector();
    var searchWindow = new SearchWindow();
    searchWindow.show();
}

SearchWindow = Ext.extend(Ext.Window, {
    title : '查询公共指标',
    width : 300,
    height : 290,
    layout : 'fit',
    plain : true,
    modal : true,
    bodyStyle : 'padding:10px;',
    buttonAlign : 'center',
    id : 'searchWindow',
    listeners : {
        close : function() {
            Ext.getCmp("searchWindow").destroy();
        }
    },
    initComponent : function() {

        Ext.applyIf(this, {
            items : [{
                xtype : 'form',
                // region : 'center',
                id : 'searchForm',
                // width : 270,
                bodyStyle : 'padding:10px;',
                border : false,
                labelWidth : 80,
                labelAlign : 'left',
                layout : 'form',
                url : pathUrl
                + '/selector_getPath.action',
                timeout : 600000,
                items : [
					{
						xtype : 'textfield',
						fieldLabel : '指标ID',
						allowBlank : true,
						id : 'measure_id',
						name : 'measure_id',
						anchor : '95%'
					},
                	{
                    xtype : 'textfield',
                    fieldLabel : '指标名称',
                    allowBlank : true,
                    id : 'measure_name',
                    name : 'measure_name',
                    anchor : '95%'
                },
                    new SourceTypeSelector(),
                    compSource,
					new ObjectCountPeriod(),
                ]
            }
            ],
            buttons : [{
                text : '查询',
                handler : function() {
                    var formPanel = Ext.getCmp("searchForm");
                    if (formPanel.form.isValid()){
                    	var measure_id = Ext.getCmp('measure_id').getValue();
                        var measure_name = Ext.getCmp('measure_name').getValue();
                        var peroid = null;//Ext.getCmp('objPeriodId').getValue();
                        var treePanelId = 'measureTreePanel';
                        var sourceTypeId = Ext.getCmp("sourceTypeId").getValue();
                        var measuerSource = Ext.getCmp("objSourceId").getValue();
                        searchNode(null,expandMyMeasureTreeNode,treePanelId,pathUrl+'/selector_getPath.action',
							'N',sourceTypeId,measuerSource,peroid,null,
							measure_id, measure_name);

                        _searchCount+=1;
                        _searchCount=_searchCount==_searchPaths.length?0:_searchCount;
                        _searchPath=_searchPaths[_searchCount].path.split(',');
                        expandMyMeasureTreeNode(Ext.getCmp(treePanelId).getRootNode());
					}
                }
            }, {
                text : '取消',
                handler : function() {
                    Ext.getCmp("searchWindow").destroy();
                }
            }]
        })
        EditWindow.superclass.initComponent.call(this);
    }
});

// 删除方法
function doDeleteMeasure(n) {
	Ext.Ajax.request({
				url : pathUrl + '/publicMeasure_checkHasChilren.action',
				method : 'POST',
				params : {
					measure_id : n.id
				},

				callback : function(options, success, response) {
					var json = Ext.util.JSON.decode(response.responseText);
					if (json.success) {
						Ext.MessageBox.alert("提示信息",json.info);
						return;
					}else{
						Ext.MessageBox.confirm('确认信息', '您确认要删除该指标吗?', function(btn) {
						if (btn == 'yes') {
							Ext.Ajax.request({
								url : pathUrl
										+ '/publicMeasure_common.action?method=deleteEngMeasure',
								method : 'POST',
                                timeout : 6000000,
								params : {
									measure_id : n.id
								},

								callback : function(options, success, response) {
									var json = Ext.util.JSON.decode(response.responseText);
									if (json.results[0].success) {
										propertyPanel.form.reset();
										n.remove();
									} else {
											showFailureData(n.id,eval(json.results[0].info));
									}
								}
							});
                            //调用删除指标存储过程
                            Ext.Ajax.request({
                                url : pathUrl + '/publicMeasure_common.action?method=engMeasureExeProc',
                                method : 'POST'
                            });
						}
					});
					}
				}
			});
}

// 添加运算依赖指标
function addDependMeasure(node) {
	var tmpId = selectNodeId;
	Ext.Ajax.request({
		url : pathUrl + '/publicMeasure_common.action?method=addDependMeasure',
		method : 'POST',
		params : {
			parent_measure_id : selectNodeId,
			measure_id : node.id
		},

		callback : function(options, success, response) {
			var json = Ext.util.JSON.decode(response.responseText);
			if (json.success) {
				node.getOwnerTree().getNodeById(tmpId).select();
				store.reload();
			} else {
				Ext.MessageBox.alert('错误', json.info);
			}
		}
	});
}

// 删除运算依赖指标
function removeDependMeasure(measure_id, measure_name) {
	var message = '是否确认指标:' + selectNode.attributes.measure_name + '['
			+ selectNode.id + '] 的计算不依赖指标:' + measure_name + '[' + measure_id
			+ '],删除后可能需要修改公式的表达式?';
	Ext.MessageBox.confirm("确认信息", message, function(btn) {
		if (btn == 'yes') {
			Ext.Ajax.request({
				url : pathUrl
						+ '/publicMeasure_common.action?method=removeDependMeasure',
				method : 'POST',
				params : {
					parent_measure_id : selectNodeId,
					measure_id : measure_id
				},
				callback : function(options, success, response) {
					var json = Ext.util.JSON.decode(response.responseText);
					if (json.success) {
						store.reload();
					} else {
						Ext.MessageBox.alert('错误', json.info);
					}
				}
			});
		}
	});
}


function showFailureData(id,data){

	var failureData = new Ext.data.SimpleStore({
		fields:[
		{name:'dependId',type:'string'},
		{name:'dependName',type:'string'},
		{name:'dependOwnerBankID',type:'string'},
		{name:'dependOwnerBankName',type:'string'}
		]
	})
	failureData.loadData(data);

	var gridPanel = new Ext.grid.GridPanel({
		region : 'center',
		store : failureData,
		columns : [
		new Ext.grid.RowNumberer(),
		{id:'dependId',header:'依赖实体ID',width:180,sortable:true,dataIndex:'dependId'},
		{id:'dependName',header:'依赖实体名称',width:180,sortable:true,dataIndex:'dependName'},
		{id:'dependOwnerBankID',header:'归属机构ID',width:100,sortable:true,dataIndex:'dependOwnerBankID'},
		{id:'dependOwnerBankName',header:'归属机构名称',width:100,sortable:true,dataIndex:'dependOwnerBankName'}
		]
	})

	var win = new Ext.Window({
		width:600,
		height:300,
		items:[gridPanel],
		buttonAlign : 'center',
		layout:'border',
		buttons:[{
			text:'确定',
			handler : function () {
				win.destroy();
			}
		}]
	})
	win.setTitle("指标为["+id+"]存在以下依赖关系");
	win.show();

}

