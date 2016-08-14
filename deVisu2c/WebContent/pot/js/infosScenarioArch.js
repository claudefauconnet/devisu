// depends on infosAbstract.js

var queryParams = getQueryParams(document.location.search);
var dbName = queryParams.dbName;
var currentScenario = {};
var bbIds = [];
var currentbbId;
var modified=false;

$(function() {
	// $("#attrsTabMessage", window.parent.document).html("");
	setScenariosSelect();

});

function setScenariosSelect() {

	var useCasesTechnos = {};
	var scenariosData = proxy_loadData(dbName, "scenarios", {});

	scenariosData.sort(function(a, b) {
		if (a.name < b.name)
			return -1;
		if (a.name > b.name)
			return 1;
		return 0;
	});
	scenariosData.splice(0, 0, "");

	fillSelectOptions(scenarioSelect, scenariosData, "name", "id");
	

}

function loadScenario() {
	if(currentScenario && modified){
		if(confirm("Do you want to save current scenario before changing")){
			saveScenarioBuildingBlocks();
		}
		modified=false;
	}
		
	$("#buidingBlocksSpan").html("");
	var SC_id = parseInt($("#scenarioSelect").val());
	loadScenarioBuildingBlocks(SC_id);
}

function loadScenarioBuildingBlocks(scenarioId) {
	currentScenario = proxy_loadData(dbName, "scenarios", {
		id : scenarioId
	})[0];
	window.parent.currentObjectId=currentScenario.id;
	window.parent.srcIsLoaded=true;
	window.parent.setRadarLabel(currentScenario.name,"axisX");
	var bbs = currentScenario.buildingBlocks;
	if (!bbs)
		bbs = [];
	bbIds = [];
	for (var i = 0; i < bbs.length; i++) {
		addBuildingBlockToPage(bbs[i].id);
		bbIds.push(bbs[i].id);
	}

	highlightBuidingBlocksOnArchMap(bbIds);

}

function highlightBuidingBlocksOnArchMap(bbs) {
	window.parent.forcePointColor(bbs, "blue");

}

function execute(objId) {
	currentObjectId = objId;
	var scenario = $("#scenarioSelect").val();
	if (scenario == "") {
		return;
		// alert("choose a scenario first");
	} else {
		modified=true;
		bbIds.push(objId);
		addBuildingBlockToPage(objId,true);

	}
}


function addBuildingBlockToPage(BB_id, isNewFromMap) {

	var bbs = proxy_loadData(dbName, "buildingBlocks", {
		id : BB_id
	});
	if (bbs.length > 0) {
		drawBuildingBlockBox(bbs[0]);
		if (!currentScenario.buildingBlocks) {
			currentScenario.buildingBlocks = [];

		}
		if(isNewFromMap)
			currentScenario.buildingBlocks.push({id:BB_id,comment:"",name:bbs[0].name});
		currentbbId = BB_id;
		window.parent.forcePointColor(bbIds, "blue");
	}
}

function drawBuildingBlockBox(bbObj) {
	var checked = "checked='checked'";
	var CbxText = "<input class='cbx-bb' onchange='onBbCbxChange(this)' type='checkBox' "
			+ checked + "label='" + bbObj.name + "' id='" + bbObj.id + "'>";
	var bbCcommentButton = "<button onclick='getbbComment(" + bbObj.id
			+ ")'>Comment...</div>";
	
	var bbCcommentButton ="<img onclick='getbbComment(" + bbObj.id+ ")' src='images/info.jpg' width='15px'>"
	
	var text = "<li>" + CbxText + "<font color='blue'>" + bbObj.name
			+ "</font> " + bbCcommentButton + "</li>";
	$("#buidingBlocksSpan").append(text);
}

function onBbCbxChange(cbx) {
	var bbId = parseInt(cbx.id);
	if ($(cbx).prop("checked")) {
		bbIds.push(bbId);
	} else {
		bbIds.splice(bbIds.indexOf(bbId), 1);
	}
	window.parent.forcePointColor(bbIds, "blue");
}

function saveScenarioBuildingBlocks() {
	
	var cbxs = $(".cbx-bb");
	var newBbs = [];
	for (var i = 0; i < cbxs.length; i++) {
		if (cbxs[i].checked) {// ajout des technos à chaque dc
			var BB_id = parseInt(cbxs[i].id);
			var BB_name = cbxs[i].label;
			for (var j= 0; j < currentScenario.buildingBlocks.length; j++) {
				var bb = currentScenario.buildingBlocks[j];
				if (bb.id == BB_id) {
					newBbs.push(bb);
				}
			}

		}
	}
	currentScenario.buildingBlocks = newBbs;
	proxy_updateItem(dbName, "scenarios", currentScenario);
	generateSVG();
	modified=false;

}

function showAllbuidingBlocks() {

	window.parent.resetAllPointsOpacity(1);
}

function addBuidlingBlock() {
	var bbName = prompt("Enter new building block name");
	if (bbName && bbName.length > 0) {
		proxy_addItem(dbName, "buildingBlocks", {
			name : bbName
		});
		window.parent.reloadRadar();
	}

}

function getbbComment(bbId) {

	
	currentbbId = bbId;
	var comment = "";
	for (var i = 0; i < currentScenario.buildingBlocks.length; i++) {
		var bb = currentScenario.buildingBlocks[i];
		if (bb.id == bbId) {
			comment = currentScenario.buildingBlocks[i].comment;
			if (!comment)
				comment = "";
		}
	}
	$("#bbComment").val(comment);

}



function setbbComment() {
	modified=true;
	var comment = $("#bbComment").val();
	if (currentScenario && !currentScenario.buildingBlocks) {
		currentScenario.buildingBlocks = [];
	}
	if (comment && currentScenario && currentbbId) {
	
		for (var i = 0; i < currentScenario.buildingBlocks.length; i++) {
			var bb = currentScenario.buildingBlocks[i];
			if (bb.id == currentbbId) {
				currentScenario.buildingBlocks[i].comment = comment;
			}
		}
	}

}

function generateSVG(){
	var svg=window.parent.getSVG();
	if(!currentScenario){
		alert("select object first");
		return;
	}
	
	var jsonFields={svg:svg};
	proxy_updateItemFields(dbName, "scenarios", {id:currentScenario.id}, jsonFields) 
	

}
