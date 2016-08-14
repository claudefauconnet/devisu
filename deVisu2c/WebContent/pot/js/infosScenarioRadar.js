var queryParams = getQueryParams(document.location.search);
var dbName = queryParams.dbName;
var currentObjectId = parseInt(queryParams.objectId);
var currentScenario = {};

$(function() {
if(currentObjectId)
	execute(currentObjectId);

});

function execute(objId) {
	currentObjectId=objId;
	$("#idSpan").html(objId)
	fillAccordion(objId);

}

function fillAccordion() {
	var SCdata = proxy_loadData(dbName, "scenarios", {
		id : currentObjectId
	});

	if (SCdata.length > 0)
		currentScenario = SCdata[0];

	var UCData = proxy_loadData(dbName, "use_cases", {
		scenario_id : currentObjectId
	});

	var ucTechnos = [];
	var ucDCs = [];
	for (var i = 0; i < UCData.length; i++) {
		var currentUseCase = UCData[i]

		if (!currentUseCase.dcs)
			continue;

		for (var j = 0; j < currentUseCase.dcs.length;j++) {

		var dcName = currentUseCase.dcs[j].dc;
			if ($.inArray(dcName, ucDCs) < 0) {
				ucDCs.push(dcName);
			}

			var dcTechnos = currentUseCase.dcs[j].technos;
			if (!dcTechnos)
				continue;

			for (var k = 0; k < dcTechnos.length; k++) {
				var technoName = dcTechnos[k].name;
				if ($.inArray(technoName, ucTechnos) < 0) {
					ucTechnos.push(technoName);
				}
			}
			;
		}
	}

	$("#nameSpan").html(currentScenario.name);
	$("#descriptionSpan").html(currentScenario.description);

	var str = "";
	for (var i = 0; i < UCData.length; i++) {
		str += "<b>"+UCData[i].name+"</b><br><font >-["+ UCData[i].bu+"/"+ UCData[i].BD+"/"+ UCData[i].BC+ "]</font>" + ";";
	}
	$("#UCsSpan").html(
			"<ul><li>" + str.replace(/;/g, '</li><li>') + "</li></ul>");

	var str = "";
	for (var i = 0; i < ucDCs.length; i++) {
		str += ucDCs[i] + ";";
	}
	$("#DCsSpan").html(
			"<ul><li>" + str.replace(/;/g, '</li><li>') + "</li></ul>");

	var str = "";
	for (var i = 0; i < ucTechnos.length; i++) {
		str += ucTechnos[i] + ";";
	}
	$("#technosSpan").html(
			"<ul><li>" + str.replace(/;/g, '</li><li>') + "</li></ul>");

	$("#accordion").accordion();

}
