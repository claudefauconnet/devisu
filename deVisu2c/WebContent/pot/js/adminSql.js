var sqlResult;



function execSql() {
	var dbName = $("#dbSelect").val();
	var sqlConn = $("#sqlConnSelect").val();
	var sqlRequest = $("#sqlRequest").val();
	sqlResult = proxy_execSql(dbName, sqlConn, sqlRequest);
	if ($.isArray(sqlResult)) {
		showSqlResult(sqlResult);
	}

}


function showSqlResult(result) {
	var headers = Spreadsheet_GetHeader(result);
	fillSelectOptions(sqlFields, headers);
	var sheet = $(spreadsheetAdminSql).handsontable({

		data: result,
		// colWidths: [55, 80, 80, 80, 80, 80, 80], //can also be a number or a
		// function
		rowHeaders: true,
		colHeaders: true,
		minRows: 200,
		// fixedColumnsLeft: 2,
		// fixedRowsTop: 2,
		minSpareRows: 1,
		stretchH: 'all',
		columnSorting: true,
		contextMenu: true
	});

}


function addFieldToImport(field) {
	if(!field)
	 field = $("#sqlFields").val();
	if (field !== "") {
		$("#sqlFieldsToImport").append($("<option>", {
			value: field,
			text: field
		}));
	}

	var str='#sqlFields option[value="'+field+'"]';
	$(str).remove();
}


function removeFieldToImport(){
	var field=$("#sqlFieldsToImport").val();
	if (field != "") {
		$("#sqlFields").append($('<option>', {
			value: field,
			text: field
		}));
	}

	$('#sqlFieldsToImport option[value="'+field+'"]').remove();

}
function importSqlToMongo() {
	var dbName = $("#dbSelect").val();
	var fieldsToImport = [];
	$("#sqlFieldsToImport option").each(function() {
		fieldsToImport.push($(this).val());
	});
	var collectionName = $("#sqlImportCollectionName").val();
	if(!collectionName || collectionName==""){
		alert("enter collection name");
		return;
	}
	if (!confirm("Import data in collection " + collectionName + " of database " + dbName))
		return;

	var jsonData = [];
	for (var i = 0; i < sqlResult.length; i++) {
		var obj = {};
		for (var j = 0; j < fieldsToImport.length; j++) {
			var val = sqlResult[i][fieldsToImport[j]];
			obj[fieldsToImport[j]] = val;
		}
		jsonData.push(obj);
	}

	proxy_addItems(dbName, collectionName, jsonData)


}

function loadConnections() {
	dbName = $("#dbSelect").val();
	var sqlConns = proxy_loadData(dbName, 'admin', {
		type: "sqlConn"
	});
	sqlConns.splice(0, 0, "");
	for (var i = 0; i < sqlConns.length; i++) {
		$("#sqlConnSelect").append($('<option/>', {
			value: sqlConns[i].name,
			text: sqlConns[i].name
		}));
	}

}

function loadDBs() {
	var dbs = proxy_getDBNames("admin", 'admin', {});
	dbs.splice(0, 0, "");
	for (var i = 0; i < dbs.length; i++) {
		var str = dbs[i].name;
		$("#dbSelect").append($('<option/>', {
			value: str,
			text: str
		}));
	}
}

function sqlNewImportFieldAdd() {
	var field = $("#sqlNewImportField").val();
	var val = $("#sqlNewImportFieldVal").val();
	for (var i = 0; i < sqlResult.length; i++) {
		sqlResult[i][field] = val;
	}
	showSqlResult(sqlResult);

}

