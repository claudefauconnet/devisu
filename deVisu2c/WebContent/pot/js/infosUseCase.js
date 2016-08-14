


// depends on infosAbstract.js


var queryParams = getQueryParams(document.location.search);
var dbName = queryParams.dbName;

var useCaseId = parseInt(queryParams.objectId);
var technoId = parseInt(queryParams.technoId);
var bcId = parseInt(queryParams.bcId);

var currentUseCase;
var currentTechno;
var currentBC;
var cbxTechnos = {};
var currentYear = 2016
/*
 * var currentBC; var currentBD; var currentBU;
 */

var dcsToCopy = "";
var cbxTechnos = [];


$(function() {
	if (!useCaseId)
		useCaseId=-1;
		execute(useCaseId)

});

function execute(objId){
	useCaseId=objId;
	$("#attrsTabMessage", window.parent.document).html("");
	$("#idSpan").html(objId)
	fillAccordion(objId);
	$("#accordion").accordion();
}




var useCaseDesc = {
	name : {
		"value" : "",
		type : "text",
		cols : 50,
		rows : 2

	},

	description : {
		"value" : "",
		type : "text",
		cols : 50,
		rows : 5
	},
	currentSituation : {
		"value" : "",
		type : "text",
		cols : 50,
		rows : 4
	},
}

var useCaseAttrs = {

	businessValue : {
		"value" : "",
		type : "readOnly",
	},
	horizon : {
		"value" : "",
		type : "readOnly",
	},
	priority : {
		"value" : "",
		type : "select",
		list : "priority"
	},

	riskLevel : {
		"value" : "",
		type : "select",
		list : "level",
	},

	easeOfImpl : {
		"value" : "",
		type : "select",
		list : "level"
	},

};

/*
 * var useCaseAttrs2 = { readynessLevel : { "value" : "", type : "select", list :
 * "TLR" }, impact_process_skills : { "value" : "", type : "select", list :
 * "level" }, impact_finance_businessModel : { "value" : "", type : "select",
 * list : "level" }, impact_finance_Network_alliances : { "value" : "", type :
 * "select", list : "level" }, impact_product_performance : { "value" : "", type :
 * "select", list : "level" }, impact_product_systems : { "value" : "", type :
 * "select", list : "level" }, impact_product_service : { "value" : "", type :
 * "select", list : "level" }, impact_delivery_channels : { "value" : "", type :
 * "select", list : "level" }, impact_delivery_customer : { "value" : "", type :
 * "select", list : "level" }, impact_delivery_experience : { "value" : "", type :
 * "select", list : "level" },
 *  }
 */

var selectOptionValues = {
	level : [ {
		value : 1,
		text : "low"
	}, {
		value : 2,
		text : "medium"
	}, {
		value : 3,
		text : "high"
	} ],
	TLR : [ {
		value : 1,
		text : "TLR1"
	}, {
		value : 2,
		text : "TLR2"
	}, {
		value : 3,
		text : "TLR3"
	}, {
		value : 4,
		text : "TLR4"
	}, {
		value : 5,
		text : "TLR5"
	}, {
		value : 6,
		text : "TLR6"
	}, {
		value : 7,
		text : "TLR7"
	}, ],
	priority : [ {
		value : 1,
		text : "would"
	}, {
		value : 2,
		text : "could"
	}, {
		value : 3,
		text : "should"
	}, {
		value : 4,
		text : "must"
	}, ]

}


function fillAccordion() {

	setUseCaseTechnosSelect();

	if (technoId) {
		var technoData = proxy_loadData(dbName, "technologies", {
			id : technoId
		});
		currentTechno = technoData[0];
	}

	if (useCaseId) {
		var useCaseData = proxy_loadData(dbName, "use_cases", {
			id : useCaseId
		});
		currentUseCase = useCaseData[0];
	} 

	if (bcId) {
		
		var bcData = proxy_loadData(dbName, "use_cases_tree", {
			type : "BC", id:bcId
		});
		/*var bcData = proxy_loadData(dbName, "BCs", {
			BC_id : bcId
		});*/
		var obj = bcData[0];
		currentBC={
			//	bu_id:obj.id,
				bu:obj.data.obj.bu,
				BD : obj.data.obj.BD,
				BC : obj.data.obj.BC,
				BC_id : obj.data.obj.BC_id,
				BD_id : obj.data.obj.BD_id
				
		}
		console.log(bcId, currentBC)
	}

	setOldUseCaseDCsAndTeshnos();
	setAttributesValue(useCaseAttrs);
	// setAttributesValue(useCaseAttrs2);
	setAttributesValue(useCaseDesc);
	drawAttributes();
	drawDescription();
	if (currentUseCase) {
		$("#idSpan").html(currentUseCase.id);

		$("#nameRead").html(currentUseCase.name);
	}

	

}


function drawDescription() {
	var str = "<table>"
	for ( var key in useCaseDesc) {
		strVal = useCaseDesc[key].value;
		str += "<tr><td>" + key + "</td></tr><tr><td>" + strVal + "</td></tr>";
	}
	str += "</table>";
	$("#descriptionDiv").html(str);
}

function setUseCaseTechnosSelect() {

	var useCasesTechnos = {};
	var technos = proxy_loadData(dbName, "technologies", {});

	technos2 = [];
	for (var i = 0; i < technos.length; i++) {
		if (!technos[i].id) {
			technos[i].id = technos[i].id;

		}

		if (!useCasesTechnos["_" + technos[i].id]) {
			technos2.push(technos[i]);
			useCasesTechnos["_" + technos[i].id] = technos[i];
		}

	}
	technos2.sort(function(a, b) {
		if (a.name < b.name)
			return -1;
		if (a.name > b.name)
			return 1;
		return 0;
	});
	technos2.splice(0, 0, "");

	fillSelectOptions(useCaseTechnologiesSelect, technos2, "name", "id");
	if (technoId) {

		for (var i = 0; i < technos2.length; i++) {
			if (technos2[i].id == technoId) {
				useCaseTechnologiesSelect.options.selectedIndex = i;
				break;
			}

		}
		// onUseCaseTechnologiesSelected(useCaseTechnologiesSelect);
	}
}
function setNewUseCaseDCsAndTechnos() {
	var technoId = parseInt($("#useCaseTechnologiesSelect").val());
	var dcData = proxy_loadData(dbName, "r_techno_dc", {
		techno_id : technoId
	});

	var DCtext = "<ul>";
	var checked = "";
	var techsSrtr = "";
	var technoObjs = [];
	// cbxTechnos = {};
	// cbxTechnos = [];
	var techIds = [];
	for (var i = 0; i < dcData.length; i++) {
		if ($.inArray(dcData[i].techno_id, techIds) < 0) {
			technoObjs.push({
				name : dcData[i].techno_name,
				id : dcData[i].techno_id
			});
			techIds.push(dcData[i].techno_id);
			techsSrtr += dcData[i].techno_name;
		}

		/*
		 * if (techsSrtr.length > 0) techsSrtr = techsSrtr.substring(0,
		 * techsSrtr.length - 1);
		 */
		var cbxid = "dc_cbx_" + dcData[i].dc_id;
		var li_id = "dc_cbx_" + dcData[i].dc_id;
		cbxTechnos[cbxid] = technoObjs;
		var cbxEvt = "onclick='onNewCbxClicked(this)'";
		var CbxText = "<input class='cbx-dc cbx-dc-new' type='checkBox' "
				+ cbxEvt + "  " + checked + "label='" + dcData[i].dc_name
				+ "' id='" + cbxid + "'>";

		DCtext += "<li id='li_" + cbxid + "' class='liNew'><font color='blue'>"
				+ CbxText + dcData[i].dc_name + "</font><font color='green'>["
				+ techsSrtr + "]</font>" + "</li>";

	}
	DCtext += "</ul>";

	$("#ulNewDcs").html(DCtext);
	// if(currentUseCase){//} && ($("#oldUseCaseDsAndTechnos").html()==""))
	/*
	 * if (currentUseCase) setOldUseCaseDCsAndTeshnos(currentUseCase.dcs);
	 */
	// }
}

function setOldUseCaseDCsAndTeshnos() {
	if (!currentUseCase || !currentUseCase.dcs)
		return;
	var oldLinkedDcsAndTechnos = currentUseCase.dcs;
	$("#ulOldDcs").html(dcsToCopy);
	dcsToCopy = "";

	var DCtext = "";

	if (!oldLinkedDcsAndTechnos)
		return;

	for (var i = 0; i < oldLinkedDcsAndTechnos.length; i++) {
		var checked = "checked='checked'";
		var techsSrtr = "";
		var technoObjs = [];
		var techIds = [];

		var techs = oldLinkedDcsAndTechnos[i].technos;
		if (!techs)
			continue;

		for (var k = 0; k < techs.length; k++) {
			if ($.inArray(techs[k].id, techIds) < 0) {
				technoObjs.push({
					name : techs[k].name,
					id : techs[k].id
				});
				techIds.push(techs[k].id);
				techsSrtr += techs[k].name + ",";
			}

		}
		if (techsSrtr.length > 0)
			techsSrtr = techsSrtr.substring(0, techsSrtr.length - 1);

		var cbxid = "dc_cbx_" + oldLinkedDcsAndTechnos[i].dc_id;

		cbxTechnos[cbxid] = technoObjs;
		var cbxEvt = "";

		var CbxText = "<input class='cbx-dc'  type='checkBox' " + checked
				+ "label='" + oldLinkedDcsAndTechnos[i].dc + "' id='" + cbxid
				+ "'>";

		DCtext += "<li><font color='blue'>" + CbxText
				+ oldLinkedDcsAndTechnos[i].dc
				+ "</font><font color='green'><B>[" + techsSrtr
				+ "]</B></font>" + "</li>";

	}
	// DCtext += "</ul>";

	// $("#oldUseCaseDsAndTechnos").append(DCtext);
	$("#ulOldDcs").append(DCtext);

}
function onNewCbxClicked(cbx) {
	var liCbx = $("#li_" + cbx.id);
	$(cbx).attr("class", "cbx-dc");
	var xxx = liCbx;
	jQuery(liCbx).detach().prependTo('#ulOldDcs')

}



function saveUseCase() {
	if (!currentUseCase)
		currentUseCase = {};
	// var currentBC = currentBC;

	var cbxDcs = $(".cbx-dc");

	var radarUsecaseName = $("#attr_name").val();
	if (radarUsecaseName == "") {
		alert("name is mandatory");
		return;
	}

	currentUseCase = setModifiedValues(currentUseCase, ".useCaseDesc");
	currentUseCase = setModifiedValues(currentUseCase, ".objAttr");

	var dcs = [];
	var saveOK = false;
	for (var i = 0; i < cbxDcs.length; i++) {
		if (cbxDcs[i].checked) {// ajout des technos à chaque dc
			saveOK = true;
			var technos = cbxTechnos[cbxDcs[i].id];
			// console.log(JSON.stringify(technos));
			var addTechnoToDc = true;

			dcs.push({
				dc_id : parseInt(cbxDcs[i].id.substring(7)),
				dc : cbxDcs[i].attributes.label.value,
				technos : technos,

			});

		}
	}
	if (saveOK == false) {
		// alert("cannot save if no digital capabililty selected");
		// return;
	}

	if (currentUseCase.BC_id) {// update
		currentUseCase.dcs = dcs;
		proxy_updateItem(dbName, "use_cases", currentUseCase);

	} else {// new.
		//currentUseCase.bu_id = currentBC.bu_id;
		currentUseCase.bu = currentBC.bu;
		currentUseCase.year = currentYear;
		currentUseCase.BD = currentBC.BD;
		currentUseCase.BC = currentBC.BC;
		currentUseCase.BC_id = currentBC.BC_id;
		currentUseCase.BD_id = currentBC.BD_id;
		currentUseCase.dcs = dcs;

		var newObj = proxy_addItem(dbName, "use_cases", currentUseCase);
		/*
		 * var childTreeNode2 = jQuery.extend(true, {}, childTreeNode);
		 * addChildToTree(null, childTreeNode, 1);
		 * treeData.push(childTreeNode2); saveUseCasesTree(treeData);
		 */

		currentUseCase.id = newObj.id;
		newUseCase = currentUseCase;
		window.parent.addItemToTree(window.parent.parentTreeNode,
				currentUseCase, "UC");
		
		// addItemToTree(currentTreeNode, useCase, "UC");

	}
	window.parent.reloadRadar();
	// hoverHide();
	parent.closeIFrameTechnosInfo(currentUseCase);
	$("#attrsTabMessage", window.parent.document).html("uses case saved");
	// parent.updateRadarPoint(currentUseCase);
	// window.parent.hideUseCaseDetails();

}

function deleteUseCase(){
	if(confirm("do you want to delete use case "+currentUseCase.name)){
		proxy_deleteItem(dbName, "use_cases", currentUseCase.id) ;
	}
	window.parent.reloadRadar();
	$("body").html("");
	$("#attrsTabMessage", window.parent.document).html("Click a point to see its data");

}

function processTexts(technoDoc) {

	if (technoDoc.dcs) {
		var str = "";
		for (var i = 0; i < technoDoc.dcs.length; i++) {
			str += technoDoc.dcs[i].dc_name + ";";
		}
		technoDoc.dcs = str;
	}
	if (technoDoc.linkedTechnos) {
		var strWeak = "";
		var strStrong = "";
		for (var i = 0; i < technoDoc.linkedTechnos.length; i++) {
			var weak = technoDoc.linkedTechnos[i].weaklyLinkedTo_name;
			var strong = technoDoc.linkedTechnos[i].stronglyLinked_name;
			if (weak)
				strWeak += weak + ";";
			if (strong)
				strStrong += strong + ";";
		}

		technoDoc.stronglyLinkedTechs = strStrong;
		technoDoc.weaklyLinkedTechs = strWeak;
	}

	for ( var key in technoDoc) {// pour tous les champs
		var str2 = "";
		var str = "" + technoDoc[key];

		var p = 0;
		var q = 0;
		if (str.indexOf("http") > -1) {// hyperlink

			while ((p = str.indexOf("http", q + 10)) > 1) {
				var q = str.indexOf(";", p + 1);
				if (q < 0)
					q = str.length - 1;
				var href = str.substring(p, q);
				str = str.slice(0, p) + "<a href='" + href
						+ "' target='_blank'>" + "click here" + "</a>"
						+ str.slice(q);
			}
			technoDoc[key] = str;
		}

		if (str.indexOf(";") > -1) {
			// if(str.endsWith(";"))
			// str=str.substring(0,str.length-1);
			technoDoc[key] = "<ul><li>" + str.replace(/;/g, '</li><li>')
					+ "</li></ul>"
		}

		else {
			technoDoc[key] = technoDoc[key];
		}

	}

	if (technoDoc.img) {// insertion image
		technoDoc.img = "</span><img src='data/pictures/" + technoDoc.img
				+ "' width='100px' text-align='left'><span>";
	}

	// technoDoc.digitalPolarity=maturities[digitalPolarity];
	technoDoc.technologyMaturity = maturities[technoDoc.technologyMaturity];
	technoDoc.marketSkills = marketSkills[technoDoc.marketSkills];
	technoDoc.TotalSkills = TotalSkills[technoDoc.TotalSkills];

	return technoDoc;
}



