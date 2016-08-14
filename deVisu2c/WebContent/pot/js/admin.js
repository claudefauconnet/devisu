var adminObjNatureStr;
var linesToBeDeleted = [];

function loadAdminData() {
	linesToBeDeleted = [];
	adminObjNatureStr = getAdminObjNature();
	var headers = [];
	var columns = [];

	if (adminObjNatureStr == "users") {

		var roles = [ "none", "read", "restricted", "writeAll", "admin" ];
		graphData = proxy_loadData(dbName, "users", null, null);

		headers = [ "id", "login", "password", "role", "desc" ];
		colWidths = [ 50, 200, 200, 200, 400 ];
		columns = [ {
			data : "id",
			readOnly : true
		}, {
			data : "login"
		}, {
			data : "password"
		}, {
			data : "role",
			// renderer : nodeTypeRenderer,
			type : "autocomplete",
			strict : true,
			source : roles
		}, {
			data : "desc"
		} ];

	} else if (adminObjNatureStr == "natures") {
		graphData = proxy_loadData(dbName, "admin", {
			type : "nodeNature"
		}, null);
		colWidths = [ 20, 100, 50, 40, 40, 200, 40, 200 ];
		headers = [ "id", "name", "graphName", "color", "drawLabels", "linkLogic", "detailCollection", "detailFields" ];
		columns = [ {
			data : "id",
			readOnly : true
		}, {
			data : "name",

		}, {
			data : "graphName",
		}, {
			data : "color",
		}, {
			data : "drawLabels"
		}, {
			data : "linkLogic"
		}, {
			data : "detailCollection"
		}, {
			data : "detailFields"
		} ];

	} else if (adminObjNatureStr == "tabs") {
		graphData = proxy_loadData(dbName, "admin", {
			type : "tab"
		}, null);
		colWidths = [ 20, 100, 100, 100, 100, 100 ];
		headers = [ "id", "nameX", "label", "order", "params", "role" ];
		columns = [ {
			data : "id",
			readOnly : true
		}, {
			data : "name"

		}, {
			data : "label"
		}, {
			data : "order"
		}, {
			data : "params"
		}, {
			data : "role"
		}

		];
	} else if (adminObjNatureStr == "sqlConn") {
		graphData = proxy_loadData(dbName, "admin", {
			type : "sqlConn"
		}, null);
		colWidths = [ 20, 100, 150, 200, 100, 100 ];
		headers = [ "id", "name", "driver", "url", "login", "password" ];
		columns = [ {
			data : "id",
			readOnly : true
		}, {
			data : "name",

		}, {
			data : "driver",
		}, {
			data : "url"
		}, {
			data : "login"
		}, {
			data : "password"
		} ];
	} else if (adminObjNatureStr == "joins") {
		graphData = proxy_loadData(dbName, "admin", {
			type : "joins"
		}, null);
		colWidths = [ 20, 50, 100, 100, 100, 100, 100 ];
		headers = [ "id", "joinType", "targetTemplate", "sourceCollection", "sourceKey", "targetCollection", "targetKey" ];
		columns = [ {
			data : "id",
			readOnly : true
		}, {
			data : "joinType",
			type : "autocomplete",
			strict : false,
			source : [ '1-n', 'n-1', '1-1' ],

		}, {
			data : "targetTemplate",
		}, {
			data : "sourceCollection",
		}, {
			data : "sourceKey",
		}, {
			data : "targetCollection",
		}, {
			data : "targetKey"
		} ];
	} else if (adminObjNatureStr == "nodes") {
		graphData = proxy_loadData(dbName, "nodes", {}, null);
		colWidths = [ 20, 100, 50, 50 ];
		headers = [ "id", "label", "nature", "modifiedBy" ];
		columns = [ {
			data : "id",
			readOnly : true
		}, {
			data : "label",
		}, {
			data : "nature"
		}, {
			data : "modifiedBy"
		}

		];

	} else if (adminObjNatureStr == "crawlers") {
		graphData = proxy_loadData(dbName, "crawlers", {}, null);
		for (var i = 0; i < graphData.length; i++) {

		}
		colWidths = [ 20, 200, 200, 50, 250, 50, 150, 150, 200 ];
		headers = [ "id", "name", "type", "status", "url", "langage", "parser", "processor", "frequency" ];
		columns = [ {
			data : "id",
			readOnly : true
		}, {
			data : "name",
		}, {
			data : "type"
		}, {
			data : "status"
		}, {
			data : "url"
		}, {
			data : "langage"
		}, {
			data : "parser"
		}, {
			data : "processor"
		}, {
			data : "frequency"
		}

		];

	}
	/*
	 * if (graphData.length == 0) graphData = [ { A : "", B : "" } ];//
	 */

	$(spreadsheetAdmin).handsontable({
		data : graphData,
		minCols : 5,
		minRows : 10,
		minSpareRows : 1,
		colHeaders : headers,
		rowHeaders : true,
		colWidths : colWidths,
		contextMenu : [ "remove_row" ],
		columns : columns,
		columnSorting : true,
		stretchH : "all",
		search : true,
		manualColumnResize : true,
		beforeRemoveRow : deleteItem

	});
	// search
	/*
	 * $("#spreadsheetAdmin").on("keyup", function(event) { var hot =
	 * $(spreadsheetGraph).handsontable("getInstance"); var queryResult =
	 * hot.search.query(this.value); // console.log(queryResult); hot.render();
	 * });
	 */

}

function saveAdminData() {
	var withoutModifiedBy = false;
	adminObjNatureStr = getAdminObjNature();
	var collectionName = "";
	if (adminObjNatureStr == "natures" || adminObjNatureStr == "tabs" || adminObjNatureStr == "sqlConn" || adminObjNatureStr == "joins")
		collectionName = "admin";
	else if (adminObjNatureStr == "users")
		collectionName = "users";
	else if (adminObjNatureStr == "crawlers")
		collectionName = "crawlers";

	else if (adminObjNatureStr == "nodes") {
		collectionName = "nodes";
		withoutModifiedBy = true;
	}

	var handsontable = $(spreadsheetAdmin).data("handsontable");
	var data = handsontable.getData();
	// var maxId = getMaxId(data);

	for (var i = 0; i < data.length; i++) {
		var item = data[i];
		var notNullline = false; // arret sur les lignes vides
		for ( var prop in item) {
			if (item[prop] !== null && item[prop] !== "" && !$.isPlainObject(item[prop]))
				notNullline = notNullline | true;
		}
		if (notNullline === false)
			break;

		if (item.id) { // update
			if ($.inArray(item, linesToBeDeleted) < 0) {
				proxy_updateItem(dbName, collectionName, item, withoutModifiedBy);
			}

		} else { // add
			if (adminObjNatureStr == "natures")
				item.type = "nodeNature";
			if (adminObjNatureStr == "tabs")
				item.type = "tab";
			if (adminObjNatureStr == "sqlConn")
				item.type = "sqlConn";
			if (adminObjNatureStr == "joins")
				item.type = "joins";
			// item.id = ++maxId;
			proxy_addItem(dbName, collectionName, item, withoutModifiedBy);
		}
	}
	deleteRemovedItems();
}

/*
 * function getMaxId(data) { var maxId = 1; for ( var i = 0; i < data.length;
 * i++) { var item = data[i]; if (item.id) { maxId = Math.max(maxId, item.id); } }
 * return maxId;
 *  }
 */

function getAdminObjNature() {
	var dataNature = null;
	if ($("#adminObjNature_users").prop("checked"))
		dataNature = "users";
	if ($("#adminObjNature_nodes").prop("checked"))
		dataNature = "nodes";
	if ($("#adminObjNature_natures").prop("checked"))
		dataNature = "natures";
	if ($("#adminObjNature_tabs").prop("checked"))
		dataNature = "tabs";
	if ($("#adminObjNature_sqlConn").prop("checked"))
		dataNature = "sqlConn";
	if ($("#adminObjNature_joins").prop("checked"))
		dataNature = "joins";
	if ($("#adminObjNature_crawlers").prop("checked"))
		dataNature = "crawlers";

	if (!dataNature)
		alert("select a nature for data");

	return dataNature;
}

function nodeTypeRenderer(instance, td, row, col, prop, value, cellProperties) {
	Handsontable.renderers.TextRenderer.apply(this, arguments);
	var color = "#fff";
	var m = row;
	var handsontable = $(spreadsheetAdmin).data("handsontable");
	var data = graphData; // handsontable.getData();
	var sortIndex = handsontable.sortIndex;
	if (sortIndex && sortIndex.length > 0) {
		m = handsontable.sortIndex[m][0];
	}
	if (adminObjNatureStr == "users") {
		var nature = data[m].nature;
		if (nature)
			color = colors[nature];
		$(td).css({
			background : color
		});
	}
}

function deleteItem(index, amount) {
	if (index === true) // c"est comme cela : methode appelée deux fois
		// !!
		return;
	var handsontable = $(spreadsheetAdmin).data("handsontable");
	var data = graphData; // handsontable.getData();
	var sortIndex = handsontable.sortIndex;
	for (var i = 0; i < amount; i++) {
		var m = index + i;

		if (sortIndex && sortIndex.length > 0) {
			m = handsontable.sortIndex[m][0];
		}
		linesToBeDeleted.push(data[m]);
	}

}

function deleteRemovedItems() {
	var adminObjNatureStr = getAdminObjNature();
	var collectionName = "";
	if (adminObjNatureStr == "natures" || adminObjNatureStr == "tabs" || adminObjNatureStr == "sqlConn" || adminObjNatureStr == "joins")
		collectionName = "admin";
	else if (adminObjNatureStr == "users")
		collectionName = "users";
	else if (adminObjNatureStr == "nodes")
		collectionName = "nodes";
	else if (adminObjNatureStr == "crawlers")
		collectionName = "crawlers";

	for (var i = 0; i < linesToBeDeleted.length; i++) {
		proxy_deleteItem(dbName, collectionName, linesToBeDeleted[i].id);
	}

	linesToBeDeleted = [];

}


