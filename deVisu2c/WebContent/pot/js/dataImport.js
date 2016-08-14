// query synchrone
var fieldRoles = [ "label", "color", "shape", "link", "symbol", "filter", "comment" ];
var fieldTypes = [ "id", "string", "number", "enum", "date", "text", "link" ];
var shapes = [ "circle", "triangleUp", "triangleDown", "square", "star" ];

var maxAutoEnum = 11;
var dbName;
var collectionName = "technologies";


function importData() {
	var exportData = Spreadsheet_getSpreadSheetData(spreadsheetImport);
	var exportJsonArray = [];
	var colNames = exportData[0];
	newRadarName = $("#newRadarName").val();
	if (!newRadarName || newRadarName==="")
		newRadarName = prompt("enter collection name", "");
	if (!newRadarName || newRadarName==="")
		return;
	collectionName = $("#importCollectionName").val();
	if (!collectionName || collectionName==="")
		collectionName = prompt("enter collection name", "");
	if (!collectionName || collectionName==="")
		return;
	
	
	if(collectionName.indexOf("_nodes")>-1){
		if($.inArray("label",colNames)<0 || $.inArray("nature",colNames)<0){
			alert("first row should have at least  two columns with name label and nature");
			return;
		}
	}
	if(collectionName.indexOf("_links")>-1){	
		if($.inArray("source",colNames)<0 || $.inArray("target",colNames)<0){
			alert("first row should have at least  two columns with name source and target");
			return;
		}
		
	}
	

	var exportJsonArray=getJsonData();
	
	for(var i=0;i< exportJsonArray.length;i++){
		for(var j=0;j< colNames.length;j++){
		var val_=exportJsonArray[i][colNames[j]];
		if( $.isNumeric(val_))
			exportJsonArray[i][colNames[j]]=parseFloat(val_);
		}
	}

	if ( exportJsonArray.length === 0) {
		setMessage( "NO correct  data fro import","red");
		return;
	}
	//var dbs=proxy_getDBNames(dbName);
	
	proxy_addItems(newRadarName, collectionName, exportJsonArray);
	if (collectionName === "technologies"  && GenerateModelCB.checked === true) {
		dbName=newRadarName;
		initXml(exportJsonArray);
		
		
	}

}



function getJsonData(){
	var exportData = Spreadsheet_getSpreadSheetData(spreadsheetImport);
	var exportJsonArray = [];
	var colNames = exportData[0];
	
	for ( var i = 0; i < colNames.length; i++) {
		if (!colNames[i] || colNames[i] === "") {
			colNames.length = i;
		}

	}
	for ( var i = 1; i < exportData.length; i++) {
		var notNullline = false;
		var obj = {};
		for ( var j = 0; j < colNames.length; j++) {
			var colName = colNames[j];
			var value = exportData[i][j];

			if (value !== null && value !=="")
				notNullline = notNullline | true;
			obj[colName] = cleanTextForJsonImport(value);
		}
		if (notNullline === false)
			break;
		else{
			exportJsonArray.push(obj)	;
		}

	}
	return exportJsonArray;
}

/*
 * dbName = $("dbName").val(); collectionName =
 * $("#importCollectionName").val(); var maxNulllines = 3; nullLines = 1; var
 * array0 = Spreadsheet_getSpreadSheetData(spreadsheetImport); var array = [];
 * var colNames = array0[0]; for ( var i = 1; i < array0.length; i++) { var obj =
 * {}; var okCell = false; for ( var j = 0; j < colNames.length; j++) {
 * obj[colNames[j]] = cleanTextForJsonImport(array0[i][j]); okCell = okCell |
 * (array0[i][j]); } if (okCell) { array.push(obj); } else { if (nullLines++ >
 * maxNulllines) break; } } / if (!array || array.length == 0) {
 * document.getElementById('dataTextArea').value = "NO correct CSV data";
 * return; }
 * 
 * if (GenerateModelCB.checked == true) initXml(array);
 * proxy_saveData(dbName, collectionName, data); initRadarRoles(); }
 */

function initXml(data) {
	var xml = '<?xml version="1.0" encoding="UTF-8" standalone="no"?>\n';
	xml += '<radar name="' + dbName + '" multiValuedFieldSep="' + multiValuedFieldSep + '">\n';// <fields>\t';
	var obj = data[0];
	for (key in obj) {
		xml += '<field name="' + normalizeFieldName(key) + '">\n';
		xml += "\n</field>";
	}
	xml += "\n</radar>";
	var dataXml = "data=" + encodeURIComponent(xml) + "&action=saveRadarXml&dbName=" + dbName;
	proxy_saveRadarXml(dbName,xml, initRadarRoles);

	//proxy_saveDataPOST(dataXml, null, "message");
}

function finishImport(){

	initRadarRoles();
	//enrichFieldXml(field, data, xmlDoc) 
}

function enrichFieldXml(field, data, xmlDoc) {
	var enumArray = getFieldEnums(data, key);
	for ( var i = 0; i < enumArray.length; i++) {
		var role = field.getAttribute("radarRole");
		enumElt = xmlDoc.createElement("enum");
		field.appendChild(enumElt);
		enumElt.setAttribute("label", enumArray[i].label);
		if (role == "color") {
			enumElt.setAttribute("color", enumArray[i].color);
		}
		if (role == "shape") {
			enumElt.setAttribute("shape", enumArray[i].shape);
		}
	}

	if (isMultiValuedData(data, key, multiValuedFieldSep))
		enumElt.setAttribute("isMultivalued", "true");
}


function getFieldEnums(data, key) {
	var enumArray = [];
	var distinctValues = getDistinctValues(data, key, multiValuedFieldSep, maxAutoEnum);
	if (distinctValues.length > 0 && distinctValues.length < maxAutoEnum) {
		var colors = getColors("#0000FF", "#FF0000", distinctValues.length);
		/*
		 * var p = $.inArray(undefined, distinctValues); if (p > -1) colors[p] =
		 * "#ddd";
		 */

		for ( var i = 0; i < distinctValues.length; i++) {
			obj = {
				label : distinctValues[i],
				color : colors[i],
				shape : shapes[i % 4]
			};
			enumArray.push(obj);
		}
	}
	return enumArray;
}

function normalizeFieldName(name) {
	// http://www.tuxlanding.net/how-to-display-the-accented-characters-in-javascript/
	return name.replace(/ /g, '-').replace(/ /g, '-').replace(/\351/g, 'e').replace(/\350/g, 'e').replace(/\352/g, 'e').replace(/340/g, 'a');
}


function generateJson(){

	var exportJsonArray=getJsonData();
	var str=JSON.stringify(exportJsonArray);
	$("#left").css("visibility","visible");
	$("#left").html("<textarea cols='20'>"+str+"</textarea>");

}