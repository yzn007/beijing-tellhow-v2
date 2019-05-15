var selectNodeId = '';
var selectNode = null;
var engMeasureTree ;
var sourceTypeId = "";
var sourceData = null;

Ext.onReady(function() {
	var treeMenu = new Ext.Toolbar([{
		id : 'add_menu',
		text : '添加(a)',
		tooltip : '添加指标',
		iconCls : 'add',
		handler : function() {
			if (engMeasureTree.getSelectionModel().getSelectedNode() == null) {
				Ext.MessageBox.alert('提示信息', '请选择一个新增指标的父节点');
				return;
			}
			if(engMeasureTree.getSelectionModel().getSelectedNode().attributes.source_type_id != '03'
				&& engMeasureTree.getSelectionModel().getSelectedNode().id !='root'){
				Ext.Msg.alert('提示信息','只能在分类目录下添加指标!');
				return;
			}
			// var addWindow = new AddWindow();
			// addWindow.show();
			doAddLoad();
		}
	}, '-', {
		id : 'edit_menu',
		text : '编辑(e)',
		tooltip : '编辑指标',
		iconCls : 'edit',
		handler : function() {
		if (engMeasureTree.getSelectionModel().getSelectedNode() == null) {
			Ext.MessageBox.alert('提示信息', '请选择需要编辑的指标节点');
			return;
		}
		if (engMeasureTree.getSelectionModel().getSelectedNode().id == 'root')
			return;
			doEdit();
		}
	}, '-', {
        id : 'search_menu',
        text : '高级查询(s)',
        tooltip : '查询指标',
        iconCls : 'search',
        handler : function() {
            // doSearch();
            console.info("adv search")
            url = "/bsc/pages/bsc_measure_search.jsp"
            window.parent.gotoPage('search_menu','高级查询',url);
        }
    }, '-', {
		id : 'delete_menu',
		text : '删除(d)',
		tooltip : '删除指标',
		iconCls : 'delete',
		handler : function() {
			if (engMeasureTree.getSelectionModel().getSelectedNode() == null) {
				Ext.MessageBox.alert('提示信息', '请选择要删除的指标!');
				return;
			}
			if (engMeasureTree.getSelectionModel().getSelectedNode().id == 'root')
				return;

			doDeleteMeasure(engMeasureTree.getSelectionModel().getSelectedNode());
		}
	},'-', {
        id : 'import_menu',
        text : '导入(i)',
        tooltip : '导入指标',
        iconCls : 'importdata',
        handler : function() {
            doImport();
        }
    },'-',{
        text : '指标计算(p)',
        id : 'measureExe',
        toolTip : '指标计算',
        iconCls : 'publish',
        disabled : false,
        handler : function() {
            doExecuteMeasure();
        }
    }]);
	//添加树形索引
	addSearchToolbar({
		oldToolbar : treeMenu,
		expandMethod : expandMyMeasureTreeNode,
		treePanelId : 'measureTreePanel',
		is_private : 'N'
	});
	
	var rnode = getRootNode('root', '公共指标树', expandMyMeasureTreeNode);
	rnode.attributes.is_private = 'N';
	
	engMeasureTree = new Ext.tree.TreePanel({
		region : 'center',
		title : '公共指标树',
		id : 'measureTreePanel',
		tbar : treeMenu,
		animate : true,
		frame : false,
		border : true,
		loader : new Ext.tree.TreeLoader(),
		lines : false,
		listeners : {
			click : function(node) {
				propertyPanel.form.reset();
				selectNode = node;
				selectNodeId = node.id;
				if (!node || node.id == 'root'){
					selectNode = null;
					selectNodeId = '';
					return;
				};
				
				propertyPanel.form.load({
					url : pathUrl + '/publicMeasure_common.action?method=getEngMeasureById&measure_id='+node.id+'&is_private=N',
                    success:function(form ,action){
                        if (selectNode!=null){
                            var record = eval(action.result.data);
                            console.info(record);
                            var val = "";
                            //统计周期
                            switch (Ext.getCmp('countperiod').getValue()){
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
                            Ext.getCmp('countperiod').setValue(val);
                            //预警指标类型
                            switch (Ext.getCmp('alerttype').getValue()){
                                case "0":
                                    val = "阈值";
                                    break;
                                case "2":
                                    val = "复杂类型";
                                    break;
                                default:
                                    val = "";
                                    break;
                            }
                            Ext.getCmp('alerttype').setValue(val);
                            //指标来源
                            var ms_val = Ext.getCmp("measure_source").getValue();
                            for(var i = 0; i < sourceData.length; i++) {
                                if (sourceData[i][0] == ms_val) {
                                    Ext.getCmp("measure_source").setValue(sourceData[i][1]);
                                    break;
                                }
                            }
                            "预警指标" == record['source_type_desc'] ? Ext.getCmp('alerttype').show() : Ext.getCmp('alerttype').hide()
                        }
                    }
				});
			}
		},
		bodyStyle : 'padding:5px 5px',
		autoScroll : true,
		root : rnode,
		rootVisible : true
	});	
	var viewport = new Ext.Viewport({
		layout : 'border',
		items : [engMeasureTree, propertyPanel = new Ext.form.FormPanel({
				region : 'east',
				width : '320',
				title : '指标属性',
				bodyStyle : 'padding: 5px 5px,5px 12px',
				labelWidth : 95,
				labelAlign : 'top',
				layout : 'form',
				split : true,
				frame : true,
				reader : new Ext.data.JsonReader({
					root : 'results'
				},[
					{name : 'measure_id'},
					{name : 'source_id'},
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
                    {name : 'alerttype'}
				]),
                items: [
                    {
                        xtype: 'hidden',
                        name: 'obj_cate_id',
                        id: 'obj_cate_id'
                    }, {
                        xtype: 'hidden',
                        name: 'formula_desc',
                        id: 'load_formula_desc'
                    }, {
                        xtype: 'hidden',
                        name: 'source_id',
                        id: 'sid',
                        readOnly: true,
                        anchor: '95%'
                    }, {
                        xtype: 'textfield',
                        name: 'measure_id',
                        id: 'mid',
                        fieldLabel: '指标ID',
                        readOnly: true,
                        anchor: '95%'
                    }, {
                        xtype: 'textfield',
                        name: 'measure_name',
                        fieldLabel: '指标名',
                        readOnly: true,
                        anchor: '95%'
                    },

                    {
                        layout: 'column',
                        items: [{
                            columnWidth: .5,
                            layout: 'form',
                            items: [{
                                xtype: 'textfield',
                                name: 'measure_unit',
                                fieldLabel: '指标单位',
                                readOnly: true,
                                anchor: '90%'
                            }, {
                                xtype: 'textfield',
                                name: 'source_type_desc',
                                fieldLabel: '指标类型',
                                readOnly: true,
                                anchor: '90%'
                            }]
                        }, {
                            columnWidth: .5,
                            layout: 'form',
                            items: [{
                                xtype: 'textfield',
                                name: 'countperiod',
                                id: 'countperiod',
                                fieldLabel: '统计周期',
                                readOnly: true,
                                anchor: '90%'
                            }, {
                                xtype: 'textfield',
                                id: 'alerttype',
                                fieldLabel: '预警类型',
                                readOnly: true,
                                anchor: '90%',
                                hidden: true
                            }]
                        }]
                    },

                    {
                        xtype: 'textfield',
                        name: 'measure_source',
                        id: 'measure_source',
                        fieldLabel: '指标来源',
                        readOnly: true,
                        anchor: '95%'
                    }, {
                        xtype: 'textfield',
                        name: 'ohterdimension_desc',
                        fieldLabel: '其它维度',
                        readOnly: true,
                        anchor: '95%'
                    }, {
                        xtype: 'textfield',
                        name: 'districtdimension_desc',
                        fieldLabel: '地区维度',
                        readOnly: true,
                        anchor: '95%'
                    }, /*{
                        xtype : 'textfield',
                        name : 'obj_cate_desc',
                        fieldLabel : '考核对象类型',
                        readOnly : true,
                        anchor : '95%'
                    }, */{
                        xtype: 'textfield',
                        id: 'formula_expr',
                        name: 'formula_expr',
                        fieldLabel: '指标公式',
                        readOnly: true,
                        anchor: '89%'
                    }, {
                        xtype: 'textarea',
                        name: 'measure_desc',
                        fieldLabel: '指标描述',
                        readOnly: true,
                        anchor: '95%'
                    }]
            })]
	});
	
	var div=Ext.getDom('formula_expr').parentNode;
	var span=document.createElement("span");
	span.style.border="1px solid #B5B8C8";
	span.style.padding="1px 1px 1px 1px";
	span.style.verticalAlign="MIDDLE";
	span.innerHTML="<a href='javascript:doEditFormula()'><img src=\"../../public/images/icons/change.png\"></a>";
	div.appendChild(span);
	
	engMeasureTree.getRootNode().expand();
});

function beforeMeaClose(fid) {
    if(fid=='isDimension'){
        for (var i = 0; i < objDimDS.getCount(); i++) {
            var record = objDimDS.getAt(i);
            var id = record.get('link_id');
            var comp = Ext.getCmp(id);
            if(comp != null){
                comp.destroy();
            }
        }
        Ext.getCmp("objDimSet").doLayout(true);
    }else {
        for (var i = 0; i < objDistrictDim.getCount(); i++) {
            var record = objDistrictDim.getAt(i);
            var id = record.get('link_id');
            var comp = Ext.getCmp(id);
            if(comp != null){
                comp.destroy();
            }
        }
        Ext.getCmp("objDistrictDimSet").doLayout(true);
	}
}

//对象维度
var	districtDimensionStore=new Ext.data.JsonStore({
    url :pathUrl + '/selector_listDimension.action',
    root : 'results',
    totalProperty : 'totalCount',
    fields : [ 'link_id', 'link_name']

});

//对象维度
var	dimensionOtherStore=new Ext.data.JsonStore({
    url :pathUrl + '/selector_listDimension.action',
    root : 'results',
    totalProperty : 'totalCount',
    fields : [ 'link_id', 'link_name']
});


/***-----------------------------------**/
/**
 * 地区维度districtDimDS
 * objDistrictDim
 */

var objDistrictDim = new Ext.data.Store({
    proxy: new Ext.data.HttpProxy({
        url : pathUrl + '/datasourceconfig_sourceFieldList.action'
    }),
    reader: new Ext.data.JsonReader({
            root: 'results',
            id:'column_name'
        },
        [{name: 'column_name'},
            {name: 'dim_fullname'},
            {name: 'column_biz_name'},
            {name: 'link_id'},
            {name: 'data_type_id'},
            {name: 'is_tree'},
            {name: 'label_field'},
            {name: 'id_field'},
            {name: 'root_value'},
            {name: 'parent_id_field'}]),
    remoteSort: false
});

objDistrictDim.on("load",function(){
    for (var i = 0; i < objDistrictDim.getCount(); i++) {
        var record = objDistrictDim.getAt(i);
        var comp = getComponmentObj(record);
        if(comp != null){
            Ext.getCmp("objDistrictDimSet").add(comp);
        }
    }
    var comb =  Ext.getCmp("objDistrictDimSet");
    comb.on('select',function(comboBox){
		alert('ddddd');
	});
    Ext.getCmp("objDistrictDimSet").doLayout(true);
});




/**
 * 数据源类型下拉框
 */
SourceTypeSelector=function(){
	var template = new Ext.XTemplate(
   	'<tpl for="."><div style="border-top: solid 1px gray;width:100%;" class="x-combo-list-item">' 
   	+ '<p>{displayText}</p>' 
   	+ "<p><font size=2 color='green'>说明:{descript}</font></p>"
   	+ '</div></tpl>');
	var store = new Ext.data.SimpleStore({
		fields: ["retrunValue", "displayText","descript"],
		data: [
			['03','分类目录','作为指标分类的目录,不能配置公式和参与计算'],
			['00','基础指标','指标的值从数据库表中查询得出'],
			['01','衍生指标','指标的值经过其他指标加工得出'],
			['04','预警指标','预警指标分阈值范围和复杂类型'],
			/*['02','外部指标','指标的值需要从外部Excel导入'] */
		]
	});
	
	SourceTypeSelector.superclass.constructor.call(this,{
		store: store,
		valueField :'retrunValue',
		displayField:'displayText',
		mode: 'local',
		hiddenName:'source_type_id',
		editable: false,
		tpl : template,
		triggerAction: 'all',
		allowBlank:false,
		fieldLabel:'指标类型',
		name: 'source_type_id',
		value: '03',
		id:'sourceTypeId',
	    anchor:'95%',
		listeners: {
            select: function (combo, record, index) {
                if (combo.getValue() != "04") {
                    Ext.getCmp("objTypeId").hide();
                }else{
                    Ext.getCmp("objTypeId").show();
				}
                if (combo.getValue() == "03") {
                    //Ext.getCmp("objPeriodId").hide();
                    Ext.getCmp("objSourceId").hide();
                    Ext.getCmp("measure_unit").hide();
                    //Ext.getCmp("districtDimension").hide();
                    //Ext.getCmp("isDimension").hide();
                }else{
                    Ext.getCmp("objPeriodId").show();
                    Ext.getCmp("objSourceId").show();
                    Ext.getCmp("measure_unit").show();
                    Ext.getCmp("districtDimension").show();
                    Ext.getCmp("isDimension").show();
				}
            }
        }
	});

}
Ext.extend(SourceTypeSelector, Ext.form.ComboBox);

/**
 * 统计周期(年季月日)
 */


var cycleTypeDS = new Ext.data.JsonStore({
    url : pathUrl + '/selector_listProjCycleType.action',
    root : 'results',
    totalProperty : 'totalCount',
    fields : ['cycle_type_id', 'cycle_type_desc']
});
cycleTypeDS.load();

ObjectCountPeriod = function () {

    ObjectCountPeriod.superclass.constructor.call(this,{
        store: cycleTypeDS,
        valueField :'cycle_type_id',
        displayField:'cycle_type_desc',
        mode: 'local',
        hiddenName:'obj_period_id',
        editable: false,
        triggerAction: 'all',
        allowBlank:false,
        fieldLabel:'统计周期<span style="color:red;font-weight:bold" data-qtip="Required">*</span>',
        name: 'obj_period_id',
        value: '01',
        id:'objPeriodId',
        anchor:'95%'
    });
}
Ext.extend(ObjectCountPeriod,Ext.form.ComboBox);
/**
 * 指标来源
 */

sourceData = [
    ['bj01','北京市发展和改革委员会'],
    ['bj02','北京市民族事务委员会'],
    ['bj03','北京市人力资源和社会保障局'],
    ['bj04','北京市城市管理委员会'],
    ['bj05','北京市商务委员会'],
    ['bj06','北京市审计局'],
    ['bj07','北京市教育委员会'],
    ['bj08','北京市公安局'],
    ['bj09','北京市规划和国土资源管理委员会'],
    ['bj10','北京市交通委员会'],
    ['bj11','北京市旅游发展委员会'],
    ['bj12','北京市人民政府外事办公室'],
    ['bj13','北京市科学技术委员会'],
    ['bj14','北京市司法局'],
    ['bj15','北京市环境保护局'],
    ['bj16','北京市农村工作委员会'],
    ['bj17','北京市文化局'],
    ['bj18','北京市经济和信息化委员会'],
    ['bj19','北京市财政局'],
    ['bj20','北京市住房和城乡建设委员会'],
    ['bj21','北京市水务局'],
    ['bj22','北京市卫生和计划生育委员会'],
    ['bj23','北京市人民政府国有资产监督管理委员会'],
    ['bj24','北京市地方税务局'],
    ['bj25','北京市食品药品监督管理局'],
    ['bj26','北京市体育局'],
    ['bj27','北京市知识产权局'],
    ['bj28','北京市人民政府信访办公室'],
    ['bj29','北京市国有文化资产监督管理办公室'],
    ['bj30','北京市统计局'],
    ['bj31','北京市民防局'],
    ['bj32','北京市质量技术监督局'],
    ['bj33','北京市新闻出版广电局（北京市版权局）'],
    ['bj34','北京市园林绿化局'],
    ['bj35','北京市人民政府侨务办公室'],
    ['bj36','北京市安全生产监督管理局'],
    ['bj37','北京市文物局'],
    ['bj38','北京市金融工作局'],
    ['bj39','北京市人民政府法制办公室'],
    ['bj40','北京市工商行政管理局'],
    ['bj41','北京市农业局'],
    ['bj42','北京市粮食局'],
    ['bj43','北京市中医管理局'],
    ['bj44','北京市医院管理局'],
    ['bj45','北京市城市管理综合行政执法局'],
    ['bj46','北京市文化市场行政执法总队'],
    ['bj47','北京市社会建设工作办公室'],
    ['bj48','北京市人民政府天安门地区管理委员会'],
    ['bj49','北京经济技术开发区管理委员会'],
    ['bj50','北京西站地区管理委员会'],
    ['bj51','中关村科技园区管理委员会'],
    ['bj52','北京市气象局'],
    ['bj53','北京市交通管理局'],
    ['bj99','外部网站'],
    ['bj54','北京市人民政府办公厅'],
    ['bj55','12345北京市人民政府便民电话中心'],
    ['bj56','北京市应急管理局'],
    ['bj57','北京市民政局'],
    ['bj58','北京市政务服务管理局']
];
ObjectSourceFromSelector = function () {
    var store = new Ext.data.SimpleStore({
        fields: ["retrunValue", "displayText"],
        data: sourceData
    });

    ObjectSourceFromSelector.superclass.constructor.call(this,{
        store: store,
        valueField :'retrunValue',
        displayField:'displayText',
        mode: 'local',
        hiddenName:'obj_source_id',
        editable: false,
        triggerAction: 'all',
        allowBlank:true,
        fieldLabel:'指标来源',
        name: 'obj_source_id',
        value: '',
        id:'objSourceId',
        anchor:'95%',
        listeners: {
            select: function (combo, record, index) {
                if (combo.lastSelectionText != "") {
                   Ext.getCmp("measure_source_desc").setValue(combo.lastSelectionText);
                }
            }
        }
    });
}

Ext.extend(ObjectSourceFromSelector,Ext.form.ComboBox);

/**
 * 考核对象类型下拉框
 */
ObjCateSelector=function(){
	var store = new Ext.data.SimpleStore({
		fields: ["retrunValue", "displayText"],
		data: [['BM','区属部门'],['CBM','市属部门'],['ZJ','镇街']]
	});
	 
	ObjCateSelector.superclass.constructor.call(this,{
		store: store,
		valueField :'retrunValue',
		displayField:'displayText',
		mode: 'local',
		hiddenName:'obj_cate_id',
		editable: false,
		triggerAction: 'all',
		allowBlank:false,
		fieldLabel:'考核对象类型',
		name: 'obj_cate_id',
		value: 'BM',
		id:'objCateId',
	    anchor:'95%'
	});

}

Ext.extend(ObjCateSelector, Ext.form.ComboBox);

ObjAlertTypeSelector=function(){
    var store = new Ext.data.SimpleStore({
        fields: ["retrunValue", "displayText"],
        data: [['0','阈值范围'],['2','复杂类型']]
    });

    ObjAlertTypeSelector.superclass.constructor.call(this,{
        store: store,
        valueField :'retrunValue',
        displayField:'displayText',
        mode: 'local',
        hiddenName:'obj_type_id',
        editable: false,
        triggerAction: 'all',
        allowBlank:false,
        fieldLabel:'预警指标类型<span style="color:red;font-weight:bold" data-qtip="Required">*</span>',
        name: 'obj_type_id',
        value: '0',
        id:'objTypeId',
        anchor:'95%',
        listeners: {
            select: function (combo, record, index) {
                if (combo.lastSelectionText.indexOf("复杂") >=0) {
                    Ext.getCmp("measure_desc").fieldLabel = (Ext.getCmp("measure_desc").fieldLabel+
					'<span style="color:red;font-weight:bold" data-qtip="Required">*</span>');
                    Ext.getCmp("measure_desc").allowBlank = false;
                }else{
                    Ext.getCmp("measure_desc").fieldLabel = "指标描述";
                    Ext.getCmp("measure_desc").allowBlank = true;
				}
            }
        }
    });

}

Ext.extend(ObjAlertTypeSelector, Ext.form.ComboBox);

function getComponmentObj(record) {
    var is_tree = record.get('is_tree');
    var comp = null;
    comp = new objGridSelector({
        id : record.get('link_id'),
        displayFieldName : record.get('label_field'),
        valueFieldName : record.get('id_field'),
        fieldLabel : record.get('column_biz_name'),
        link_id : record.get('link_id'),
        anchor : '100%'
    });
    comp['linkId'] = record.get('link_id');

    return comp;
}


//表格下拉框
objGridSelector = function(obj) {
    var expanded = false;
    var anchor = obj.anchor?obj.anchor:'91%';
    objGridSelector.superclass.constructor.call(this,{
        id: obj.id,
        autoSelect:false,
        mode: 'local',
        triggerAction : "all",
        labelWidth : 50,
        labelAlign : 'right',
        blankText : '如需过滤数据请选择',
        linkId : obj.link_id,
        editable: false,
        fieldLabel : obj.fieldLabel,
        store: {
            xtype:'arraystore',
            fields : [obj.valueFieldName,obj.displayFieldName],
            data:[['','']]
        },
        anchor : anchor
    });



    var objCheckboxSelectionModel = new Ext.grid.CheckboxSelectionModel({
        handleMouseDown : Ext.emptyFn
    });

    var objValueColumnModel = new Ext.grid.ColumnModel([objCheckboxSelectionModel, {
        id : 'valueField',
        header : '维度编号',
        dataIndex : 'value_field'
    }, {
        id : 'valueField',
        header : '维度名称',
        dataIndex : 'display_field'

    }]);

    var objStore = new Ext.data.JsonStore({
        url : pathUrl + '/dimLink_listExpressionDetail.action?link_id='+obj.link_id,
        root : 'results',
        id : 'value_field',
        fields : ['value_field', 'display_field']
    });


    var objShow = new Ext.menu.Menu({
        items : [new Ext.grid.GridPanel({
            tbar : [{
                text : '全选',
                iconCls : "add",
                handler : function() {
                    Ext.getCmp(obj.id).setValue('');
                    Ext.getCmp(obj.id+"Grid").getSelectionModel().selectAll();
                }
            }, {
                text : '清空',
                iconCls : "delete",
                handler : function() {
                    Ext.getCmp(obj.id).setValue('');
                    Ext.getCmp(obj.id+"Grid").getSelectionModel().selectAll();
                    Ext.getCmp(obj.id+"Grid").getSelectionModel().deselectRange(0,objStore.getCount());
                    Ext.getCmp(obj.id).setRawValue('');
                }
            }],
            id : obj.id + "Grid",
            ds : objStore,
            width : 300,
            height : 280,
            cm : objValueColumnModel,
            sm : objCheckboxSelectionModel,
            loadMask : true,
            viewConfig : {
                forceFit : true
            },
            border : true
        })]
    });
    this.expand=function(){
        if(this.menu == null)
            this.menu = objShow;
        this.menu.show(this.el, "tl-bl?");
        if(!expanded) {
            expanded = true;
            Ext.getCmp(obj.id + "Grid").getSelectionModel().on("rowselect",function(sm,index,record){
                var rowVal = Ext.getCmp(obj.id).getRawValue();
                var rowText = null;
                if (obj.id.indexOf("Zone_Cd")>=0){ //地区
                    rowText = Ext.getCmp("objDistrictDimSet_desc").getValue();
				}else{//其它
                    rowText = Ext.getCmp("objDimSet_desc").getValue();
				}

                if(rowVal !=null && rowVal.length >0){
                    if(rowVal.endsWith(",")==true){
                        rowVal = rowVal;
                        rowText = rowText;
                    }else{
                        rowVal = rowVal + "," ;
                        rowText = rowText + ",";
                    }
                }
                if(rowVal.indexOf(record.get('value_field')+",") == -1){
                    rowVal += record.get('value_field')+",";
                    rowText += record.get('display_field') + ",";
				}

                Ext.getCmp(obj.id).setRawValue(rowVal);
                if (rowText.indexOf("["+obj.fieldLabel+"]")<0){
                    rowText = "["+obj.fieldLabel+"]"+rowText;
				}

                if (obj.id.indexOf("Zone_Cd")>=0){ //地区
                     Ext.getCmp("objDistrictDimSet_desc").setValue(rowText);
                }else{//其它
                    Ext.getCmp("objDimSet_desc").setValue(rowText);
                }
            });
            Ext.getCmp(obj.id + "Grid").getSelectionModel().on("rowdeselect",function(sm,index,record){
                var rowVal = Ext.getCmp(obj.id).getRawValue();
                if(rowVal !=null && rowVal.length >0){
                    if(rowVal.endsWith(",")==true){
                        rowVal = rowVal;
                    }else{
                        rowVal = rowVal + "," ;
                    }
                }
                rowVal = rowVal.replace(record.get('value_field')+",","");
                Ext.getCmp(obj.id).setRawValue(rowVal)
            });

            var l = obj.id
            objStore.load({params: {link_id : l}});
        }
    };
}
Ext.extend(objGridSelector,Ext.form.ComboBox);

/**
 * 统计维度cycleDimDS
 * objDimSet
 */

var objDimDS = new Ext.data.Store({
    proxy: new Ext.data.HttpProxy({
        url : pathUrl + '/datasourceconfig_sourceFieldList.action'
    }),
    reader: new Ext.data.JsonReader({
            root: 'results',
            id:'column_name'
        },
        [{name: 'column_name'},
            {name: 'dim_fullname'},
            {name: 'column_biz_name'},
            {name: 'link_id'},
            {name: 'data_type_id'},
            {name: 'is_tree'},
            {name: 'label_field'},
            {name: 'id_field'},
            {name: 'root_value'},
            {name: 'parent_id_field'}]),
    remoteSort: false
});

objDimDS.on("load",function(){
    for (var i = 0; i < objDimDS.getCount(); i++) {
        var record = objDimDS.getAt(i);
        var comp = getComponmentObj(record);
        if(comp != null){
            Ext.getCmp("objDimSet").add(comp);
        }
    }
    Ext.getCmp("objDimSet").doLayout(true);
});

function activeMenu(b) {
	Ext.getCmp("add_menu").setDisabled(!b);
	Ext.getCmp("edit_menu").setDisabled(!b);
	Ext.getCmp("delete_menu").setDisabled(!b);
}