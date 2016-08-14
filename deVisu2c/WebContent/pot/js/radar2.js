var version = "";
var dataArray;
var currentObject;
var radarPaper;
var legendPaper;
var imgWidth;
var imgHeight;
var mode;
var drawArc;
var radarDiv;

var year = "2014";
var version = "general";
var bu = "Groupe";
var testDeCharge = false;
var multiValuedFieldSep = ",";
var currentRadarItemData;
var radarModelName;
var xmlFileName;
var collectionName;
var currentBC = null;
var showExcludedRadarPoints = false;
var technoDisplayMode = "technologies";
var currentObjectId;

var srcIsLoaded = false;
// technoDisplayMode = "pack";
// technoDisplayMode = "tree";

radarXmls = {};

function reloadRadar() {
	Radar_loadRadar(xmlFileName,radarModelName, {})
}

function Radar_loadRadar(_xmlFileName, _radarModelName, jsonQuery) {

	radarModelName = _radarModelName;

	var aRadarXml = new RadarXml(_xmlFileName, radarModelName);
	if (!aRadarXml) {
		setMessage("cannot load XML model " + _xmlFileName, "red");
		return;
	}
	radarXmls[radarModelName] = aRadarXml;
	xmlFileName=_xmlFileName;
	collectionName = radarXmls[radarModelName].collectionName;
	type = radarXmls[radarModelName].type;

	if (!jsonQuery)
		jsonQuery = {};

	if (queryParams.year)
		jsonQuery.year = parseInt(queryParams.year);
	if (queryParams.bu)
		jsonQuery.bu = queryParams.bu;
	if (queryParams.version)
		jsonQuery.version = queryParams.version;
	if (queryParams.type)
		jsonQuery.type = queryParams.type;
	if (queryParams.graph)
		technoDisplayMode = queryParams.graph;
	if (queryParams.graph)
		treeFistLevel = queryParams.treeFistLevel;

	proxy_getRadarPoints(dbName, radarModelName, collectionName, jsonQuery,
			initRadar);
	radarLoaded = true;

}

function initRadar(d) {
	{

		if (!d[0]) {
			setMessage("No radar data loaded", "red");
			return;
		}

		dataArray = d[0].points;
		if(processDataBeforeDrawingCallback){
			processDataBeforeDrawingCallback(dataArray);
		}

		if (d[0].shouldSetItemsCoordinates
				&& d[0].shouldSetItemsCoordinates == "yes") {
			$(resetItemsCoordinates).prop("checked", "checked");
		}
		setEnumIdsAndInitFilters();

		drawPoints();
		drawFilters();
		$("#radarFilterOperatorsDiv").dialog({
			autoOpen : false,
			modal : true,
			show : "blind",
			hide : "blind"
		});
	
	}
}

function drawPoints() {

	if (technoDisplayMode == "technologies") {
		drawRadar(radarDiv);
		drawLegend(radarDiv);
	} else if (technoDisplayMode == "pack") {
		$("#legend").css("visibility", "hidden");
		$("#paramsDiv").css("visibility", "hidden");
		$("#radar").css("width", "1020px");
		$("#radar").css("height", "580px");
		drawD3PackGraph();
	} else if (technoDisplayMode == "tree") {
		$("#legend").css("visibility", "hidden");
		$("#paramsDiv").css("visibility", "hidden");
		$("#radar").css("width", "1020px");
		$("#radar").css("height", "580px");
		drawD3TreeGraph();
	}
}

function loadBCs() {
	var bcs = proxy_loadData("POT2016", "BC_HD", {});
	var names = [ "" ];
	for (var i = 0; i < bcs.length; i++) {
		var str = bcs[i].Processus;
		if (str.length > 20)
			str = str.substring(0, 30) + "...";
		names.push(str);
	}
	names.sort();
	fillSelectOptions(radarBCselect, names);

}

function getRadarDescription() {

	var detailsData = proxy_loadData("POT2016", "radarDetails", {
		id : currentRadarNode.id
	});
	if (detailsData.length > 0 && detailsData[0])
		return detailsData[0].inf_corps
	else
		return "";
}

function getOtherRadarPopupInfo(data) {
	var str = "<button onclick='hoverHide()'>X</button><br>";
	var fields = getFieldsArray(data);
	for (var i = 0; i < fields.length; i++) {
		var field = fields[i];

		for ( var key in field) {
			if (key == "type")
				continue;
			if (field.type == "B") {
				str += "<B>";
			} else if (field.type == "I") {
				str += "<I>";
			}
			str += "<u>" + key + "</u>" + " : " + field[key] + "<br>";
			if (field.type == "B") {
				str += "</B>";
			}
			if (field.type == "I") {
				str += "</I>";
			}
		}

	}

	return str;
}

function getFieldsArray(data) {
	var data = proxy_loadData(dbName, "technologies", {
		id : data.id
	});
	if (data.length == 0)
		return;
	data = data[0];
	var fields = [];
	var blackList = [ "color", "x", "y", "size", "enumIds", "enumIds",
			"visible", "previousState", "size2" ];

	for ( var key in data) {
		if (blackList.indexOf(key) > -1)
			continue;
		var value = data[key];
		var obj = {};
		obj[key] = value;

		if (key == "name") {
			obj.type = "B";
			fields.splice("0", 0, obj);
		} else if (key == "id") {
			obj.type = "I";
			fields.splice("1", 0, obj);
		} else {
			fields.push(obj);
		}

	}
	return fields;
}

function getFormHTML(data) {
	var str = "<button onclick='hoverHide()'>X</button>&nbsp;";
	fields = getFieldsArray(data);
	for (var i = 0; i < fields.length; i++) {
		fieldStr = getInputField(radarXmlUrl, key, val, isTextArea, className);
		str += fieldStr + "<BR>";
	}

	return str;

}

function getDCPopupText(withDcCbx, existingUseCases) {

	var dcData = proxy_loadData("POT2016", "DC_technos", {
		radar_id : currentRadarNode.id
	});

	var str = "<button onclick='hoverHide()'>X</button>&nbsp;<b>&nbsp;&nbsp;"
			+ currentRadarNode.label + "<BR></b>" + getTechoModifiableAttrs()
			+ "</b>&nbsp;id=" + currentRadarNode.id
			+ "<hr> digital capabilities :<ul>";
	for (var i = 0; i < dcData.length; i++) {
		varCbxText = "";
		if (withDcCbx) {
			varCbxText = "<input class='cbx-dc' type='checkBox' label='"
					+ dcData[i].DC + "' id='dc_cbx_" + dcData[i].dc_id + "'>";
		}
		existingTechnosForBC = [];
		if (existingUseCases) {
			for (var j = 0; j < existingUseCases.length; j++) {
				for (var k = 0; k < existingUseCases[j].DCs.length; k++) {
					if (existingUseCases[j].DCs[k].id == dcData[i].dc_id) {
						existingTechnosForBC.push(existingUseCases[j].techno)
					}
				}
			}

		}
		str += "</b>";
		if (existingTechnosForBC.length > 0) {
			var techsSrtr = "<ul>";
			for (var j = 0; j < existingTechnosForBC.length; j++) {
				techsSrtr += "<li>" + existingTechnosForBC[j] + "</li>";
			}
			techsSrtr += "</ul>";
			str += "<li><font color='blue'><B>" + varCbxText + dcData[i].DC
					+ "</B>" + techsSrtr + "</font>" + "</li>";
		} else
			str += "<li>" + varCbxText + dcData[i].DC + "</li>";
	}
	str += "</ul>";
	str += "<hr><font color='blue'><i>" + getRadarDescription() + "</i></font>";
	return str;
}

function closeIFrameTechnosInfo(node) {
	if (node) {
		updateRadarPoint(node);
	}

	$("#technosInfoIframe").remove();
	popupHide();

}

function getTechoModifiableAttrs() {

	var str = "&nbsp;Maturity <select id='techoModifiableAttrMaturity' onchange='modifyCurrentNodeAttr(this)'>"
			+ "<option>no interest</option>"
			+ "<option>emerging</option>"
			+ "<option>adolescent</option>"
			+ "<option>first rollout</option>"
			+ "<option>mainstream</option>" + "</select>"
	str += "&nbsp;Priority <select id='techoModifiableAttrPriority' onchange='modifyCurrentNodeAttr(this)'>"
			+

			"<option>low</option>"
			+ "<option>medium</option>"
			+ "<option>high</option>" + "</select>";

	str += "<script>setTechoModifiableAttrs();</script>";
	return str;
}

function setTechoModifiableAttrs() {
	$("#techoModifiableAttrPriority").val(currentRadarNode.priority);
	$("#techoModifiableAttrMaturity").val(currentRadarNode.maturity);
}

function modifyCurrentNodeAttr(select) {
	var fieldJson = null;
	var val = "";
	if (mode != "write")
		return;
	if (select.id == "techoModifiableAttrPriority") {
		val = $("#techoModifiableAttrPriority option:selected").text();
		currentRadarNode.priority = val;
		currentRadarNode.size = val;
		fieldJson = {
			priority : val,
			size : val
		};
	} else if (select.id == "techoModifiableAttrMaturity") {
		val = $("#techoModifiableAttrMaturity option:selected").text();
		currentRadarNode.maturity = val;
		currentRadarNode.color = val;
		fieldJson = {
			maturity : val,
			color : val
		};
	}

	if (fieldJson) {
		proxy_updateItemFields(dbName, collectionName, {
			id : currentRadarNode.id
		}, fieldJson);
		updateRadarPoint(currentRadarNode);
	}

}

function addItem() {
	var json = proxy_addNewRadarItem(dbName);

	Radar_loadRadar();

}

function Radar_updateCoordinates(id, dx, dy) {
	if (dx == 0 && dy == 0)
		return;
	var obj = getObjectById(id);
	if (obj) {
		obj.x = 0 + Number(obj.x) + dx;
		obj.y = 0 + Number(obj.y) + dy;
		proxy_updateRadarCoordinates(dbName, collectionName, id, obj.x, obj.y);

	}

}

function Radar_updateComment() {
	if (!currentObject)
		return;
	var comment = document.getElementById("CF_details_textArea").value;
	var id = currentObject.id;
	var p = comment.indexOf("[");
	var q = comment.indexOf("]");
	if (p > -1 && q > -1) {
		var link = comment.substring(p + 1, q);
		var comment2 = comment.replace("[" + link + "]", "");
		currentObject.comment = comment2;
		currentObject.link = link;

	} else {
		currentObject.comment = comment;
	}

	var p = comment.indexOf("<");
	var q = comment.indexOf(">");
	if (p > -1 && q > -1) {
		var symbol = comment.substring(p + 1, q);
		var comment2 = comment.replace("<" + symbol + ">", "");
		currentObject.comment = comment2;
		currentObject.symbol = symbol;
		// updatePoint(id);

	} else {
		currentObject.comment = comment;
	}
	comment = encodeURIComponent(comment);
	proxy_updateRadarComment(dbName, collectionName, id, comment);

}

function getObjectById(id) {
	for (var i = 0; i < dataArray.length; i++) {
		if (dataArray[i].id == id) {
			currentObject = dataArray[i];
			return dataArray[i];
		}
	}
	return;
}
function getObjectDetailsId(id) {
	for (var i = 0; i < dataArray.length; i++) {
		if (dataArray[i].id == id) {
			currentObject = dataArray[i];
			return dataArray[i];
		}
	}
	return;
}

function drawRadar(radarDiv) {
	/*
	 * if (resetItemsCoordinates!=null &&
	 * $(resetItemsCoordinates).prop("checked")) {
	 * $(resetItemsCoordinates).removeAttr("checked"); if (confirm("CONFIRM :
	 * Reset points positions")) setItemsCoordinates(dataArray); }
	 */
	drawBackground(radarDiv);
	drawRadarD3(dataArray, radarDiv);

}

function drawLegend(radarDiv) {
	var emumDomElements = radarXmls[radarModelName].Xml_getLegendElements();
	var data = [];
	var currentType = "XXXXX";
	for (var i = 0; i < emumDomElements.length; i++) {
		var obj = {
			type : emumDomElements[i].getAttribute("type"),
			label : emumDomElements[i].getAttribute("label"),
			color : emumDomElements[i].getAttribute("color"),
			size : emumDomElements[i].getAttribute("size"),
		}

		data.push(obj);
	}

	drawLegendD3(data);

}

function drawLegendOld() {
	var emumDomElements = radarXmls[radarModelName].Xml_getLegendElements();
	var data = []
	for (var i = 0; i < emumDomElements.length; i++) {
		var aType = emumDomElements[i].getAttribute("type");
		if (aType != currentType) {
			if (data.length > 0)
				drawLegendD3(data);
			drawLegendType(aType);
			currentType = aType;

			data = [];
			data.push(emumDomElements[i].attributes);

		} else {
			data.push(emumDomElements[i].attributes);
		}
	}
	if (data.length > 0)
		drawLegendD3(data);

}

function toogleRWmode(cb) {
	var canMod = ($(cb).is(':checked'));
	isRadarReadOnly = !canMod;
	canModifyRadarDetails = canMod;
	$("#saveSpreadshhetButton").prop("disabled", canMod);

}
function toogleShowExcluded(cb) {
	showExcludedRadarPoints = ($(cb).is(':checked'));
	setRadarPointsVisbility();

}

function Radar_setModifyMode() {
	var elt = document.getElementById("fieldsInput");
	radarXmls[radarModelName].XML_buildFieldsTable(elt, currentObject, true);
	document.getElementById("editButtonsFormModify").style.visibility = "hidden";
	document.getElementById("editButtonsFormOK").style.visibility = "visible";
}

/*
 * function Radar_updateFields() {
 * 
 * var form = document.forms["editForm"]; var id = form.elements["id"].value;
 * var obj = getItemById(id); var id = -1; for (var i = 0; i <
 * form.elements.length; i++) { var key = form.elements[i].name; var value =
 * form.elements[i].value;
 * 
 * if (key == "id") { if (value == "") // bug du browser (Chrome et IE) id =
 * value; } if (value != "" + obj[key]) {
 * 
 * 
 * obj[key] = value; var role = XML_getFieldRole(key); if (role) { obj[role] =
 * value; } } } proxy_updateItemJsonFromRadar(dbName,collectionName, id, obj);
 * 
 * hideInfoPopup('fieldDetails');
 * document.getElementById("editButtonsFormOK").style.visibility = "hidden";
 * document.getElementById("editButtonsFormModify").style.visibility = "hidden";
 * drawRadar(); }
 */

function hideRadarPopup() {
	$("#details").css("visibility", "hidden");
	$("#radarHoverPopup").css("visibility", "hidden");
	currentRadarNode = null;
	hideRadarDetailsSpreadSheetDiv(true);
}
function showRadarData(data) {
	currentJoin = null;
	currentRadarItemData = data;
	$("#details").css("visibility", "visible");
	setLinkedObjectButtons("technologies", "linkedObjectButtons");
	importSvg("#radarDetailsDiv", "radarBase");
	showData("technologies", data.id);
}

/*
 * function showRadarDetailsData(){ $("#details").css("visibility","visible");
 * importSvg("#radarDetailsDiv","radarDetails");
 * setLinkedObjectButtons("technologies","linkedObjectButtons");
 * showData("radarDetails",currentDetailsItem.id ); }
 */

function validateRadarData() {
	saveDetailsData();
	hideRadarPopup();
}

function getPrintRadarImage() {
	var svgSrc = getRadarImg();
	console.log(svgSrc);
	canvg('canvas', svgSrc);
	var canvasDiv = document.getElementById("canvas");
	var img = canvasDiv.toDataURL("image/png");
	img = img.replace(/^data:image\/png;base64,/, "");
	img += img.replace('+', ' ');
	// img=img.replace('image/png','image/octet-stream');
	// var imgBinaryArray=base64DecToArr(img);
	var imgBinaryArray = window.atob(img);
	// $("#imgBin").html(imgBinaryArray);
	// document.write('<img src="'+img+'"/>');
	var txt = "<button onclick='printRadarImage()'>print</button><br><div id='imgPrintDiv'><img id='radarImage' src='data:image/png;base64,"
			+ imgBinaryArray + "'/></div>";

	$("#details").html(txt);
	$("#details").css("visibility", "visible");

}
function printRadarImage() {
	var www = $('#imgPrintDiv');
	$('#imgPrintDiv').printElement();

}

function removePoint(id) {
	for (var i = 0; i < dataArray.length; i++) {
		if (dataArray[i].id == id) {
			dataArray[i].deleted = true;
		}
	}
	drawRadar();

}

function onClickPointInRadar(obj) {
	if (srcIsLoaded === false) {
		var str = radarXmls[radarModelName].infosPage;

		if (!str) {
			console.error("no infoPage defined in xml config file");
			return;
		}
		str += "?objectId=" + obj.id + "&dbName=" + dbName;

		$(attrsIFrame).attr("src", str);
		$('#tabs-radarLeft').tabs({
			active : 2
		});
		srcIsLoaded = true;
	} else {
		onCtlClickPointInRadar(obj);
	}

}

function onCtlClickPointInRadar(obj) {
	$(attrsIFrame).prop('contentWindow').execute(obj.id);

}
function onAltClickPointInRadar(obj) {
	// $(attrsIFrame).prop('contentWindow').execute(obj);

}

function generateSVG(){
$(attrsIFrame).prop('contentWindow').generateSVG();
}
