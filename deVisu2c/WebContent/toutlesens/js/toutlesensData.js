//function toutlsensDataClass(){

var excludeLabels = {};
var cachedResultArray;
var cachedResultArray2;

var isZoomed = false;
var hoverRect;
var hoverText;
var legendNodeLabels = {}
var legendRelTypes = {};


function getNodeAllRelations(id,output,addToExistingTree,cacheNewResult) {
	 legendNodeLabels = {}
	 legendRelTypes = {};
	var subGraphWhere = ""
	if (subGraph)
		subGraphWhere = " and node1.subGraph=\"" + subGraph + "\" "
		// http://graphaware.com/graphaware/2015/05/19/neo4j-cypher-variable-length-relationships-by-example.html

		/*
		 * var statement = "MATCH path=(node1:technos {id:"+id+"})-[*..100]->() " +
		 * "RETURN relationships(path) as rels,nodes(path)as nodes limit 100"
		 */

	var numberOfLevelsVal = $("#numberOfLevels").val();
	// var statement = "MATCH path=(node1:"
	// + currentLabel
	var statement = "MATCH path=(node1"
			+ ")-[*.."
			+ numberOfLevelsVal
			+ "]-() where ID(node1)="
			+ id
			+ subGraphWhere
			+ " RETURN EXTRACT(rel IN relationships(path) | type(rel))as rel,nodes(path)as nodes, EXTRACT(node IN nodes(path) | ID(node)) AS ids, EXTRACT(node IN nodes(path) | labels(node))  limit 500"
	console.log(statement);
	var payload = {
		"statements" : [ {
			"statement" : statement
		} ]
	};
	paramsObj = {
		mode : "POST",
		urlSuffix : "db/data/transaction/commit",
		payload : JSON.stringify(payload)
	}

	$.ajax({
		type : "POST",
		url : "neoapi",
		data : paramsObj,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {

			if (!data || data.length == 0) {
				setMessage("No results", blue);
				return;
			}
			var resultArray = data.results[0].data;
			if(addToExistingTree && cachedResultArray ){
				
				resultArray=$.merge(resultArray,cachedResultArray);
				
				
			}
			cachedResultArray = resultArray;
			var json = toFlareJson(resultArray,addToExistingTree);
			
			if(output && output=="tree"){
				if (!d3tree)
					d3tree = new D3Tree2($("#graphDiv"));
				d3tree.drawTree(json);
				d3tree.setInitialDisplayPosition();
			}
			else{
				drawForceCollapse(json);
			}
			drawLegend();
			var scrollLeft = ($("#graphDiv").parent().width() / 2) + 100;
			var scrollTop = ($("#graphDiv").parent().height() / 2) - 100;
			$("#graphDiv").parent().scrollLeft(scrollLeft);
			$("#graphDiv").parent().scrollTop(scrollTop);
			// drawRotatingTree(json)

		},
		error : function(xhr, err, msg) {
			console.log(xhr);
			console.log(err);
			console.log(msg);
		}

	});
	showInfos2(id);

}



function showInfos2(id) {
	query = "MATCH (n) WHERE ID(n) =" + id
			+ " RETURN n,'m','r',labels(n),'x',ID(n) limit 1 ";

	executeQuery(QUERY_TYPE_MATCH, query, function(d) {
		showInfosCallback(d);

	});

}

function toFlareJson(resultArray,addToExistingTree) {
	var rootId;
	if (!resultArray) {
		resultArray = cachedResultArray;
	} else {
		
	}
	var nodesMap = {};

	for (var i = 0; i < resultArray.length; i++) {
		var rels = resultArray[i].row[0];
		var nodes = resultArray[i].row[1];
		var ids = resultArray[i].row[2];
		var labels = resultArray[i].row[3];
		var legendRelIndex = 1;
		for (var j = 0; j < nodes.length; j++) {


			var nodeNeo = nodes[j];

		
			var nodeObj = {
				name : nodeNeo.nom,

				myId : nodeNeo.id,
				type : labels[j][0],
				id : ids[j],
				children : []
			}
			if (j > 0 && excludeLabels[nodeNeo.type] > 0)
				continue;
			
			if (j== 0) {	
				nodeObj.parent = "root";
				rootId=nodeObj.id;
				nodesMap.root = nodeObj

			}

			else {
				
				
				nodeObj.relType = rels[j - 1];
				nodeObj.parent = ids[j - 1];
				nodesMap[nodeObj.id] = nodeObj;
			}
			
			
			if (!legendRelTypes[nodeObj.relType]) {
				legendRelTypes[nodeObj.relType] = {
					type : nodeObj.relType,
					index : legendRelIndex++
				}
			}
			if (!legendNodeLabels[nodeObj.type]) {
				legendNodeLabels[nodeObj.type] = {
					type : nodeObj.type
				}
			}
		// console.log(JSON.stringify(nodeObj));
		// console.log(","+i+","+j+","+nodeObj.name+","+nodeObj.id+","+
		// nodeObj.parent);
		}

	}	
	deleteRecursiveReferences(nodesMap);
	var root = nodesMap.root;
	root.isRoot = true;
	addChildRecursive(root, nodesMap, 1);
	
	
	// console.log (JSON.stringify(root));
	return root;
}

// eliminer les references circulaires
function deleteRecursiveReferences(nodesMap){
	var idsToDelete=[];
	for(var key in nodesMap){
		var parent=nodesMap[key].parent;
		if(nodesMap[parent])
		console.log( nodesMap[parent].parent+"  "+nodesMap[key].id)
		
		if(nodesMap[parent] && nodesMap[parent].parent==nodesMap[key].id){
			// if(nodesMap[key].parent!=rootId)// on ne detruit pas les noeud
			// centraux
				idsToDelete.push(nodesMap[key].id);
			}
		
	}
	for(var i=0;i<idsToDelete.length;i++){
		delete nodesMap[idsToDelete[i]];
		break;
	}
	
}

function addChildRecursive(node, nodesMap, level) {
	try {// max stack size limit
		for ( var key in nodesMap) {

			var aNode = nodesMap[key];

			if (aNode.parent == node.id) {
				if (!nodesMap[key].visited) {
					aNode.level = level
					node.children.push(aNode);
					addChildRecursive(aNode, nodesMap, level + 1);
					nodesMap[key].visited = true;
				} else {
					;// console.log(node.name);
				}
			}

		}
	} catch (e) {
		console.log(e);
	}

}

/**********************************************Tree*****************************************************************************************************************
********************************************************************************************************************************************************************
********************************************************************************************************************************************************************/
function prepareTreeData(neoResult) {
	$("#spreadSheetDiv").css("visibility", "hidden");
	showInfosCallback(neoResult);
	var nameLength = 30;
	var data = neoResult[0].data;
	var variables = neoResult[0].columns;

	if (variables.length < 2)
		return;

	var nodes = {};
	setMessage(data.length + " rows retrieved", green);
	var distinctParentNodes = {};
	var parentsVar = 0;
	if (currentVariable)
		parentsVar = variables.indexOf(currentVariable);
	// selection des noeuds parents
	for (var i = 0; i < data.length; i++) {
		var row = data[i].row;
		var obj = row[parentsVar];
		if (!distinctParentNodes[obj.id]) {
			distinctParentNodes[obj.id] = {
				_id : obj.id + "_" + treeLevel,
				name : obj.name,// + " " + obj.id + "_" + treeLevel,
				type : obj.type,
				children : []
			};
		}
	}
	// remplissages des enfants

	var childrenVar = [];
	for (var i = 0; i < variables.length; i++) {
		if (variables[i] != parentsVar)
			childrenVar.push(i)
	}
	;

	for ( var key in distinctParentNodes) {

		var index = 0;
		for (var i = 0; i < data.length; i++) {
			var row = data[i].row;
			var obj = row[parentsVar];
			if (key == "" + obj.id) {
				// var childObj = row[childrenVar[0]];
				var childObj = row[1];
				distinctParentNodes[key].children.push({
					_id : childObj.id + "_" + treeLevel,
					name : childObj.name,// + " " + childObj.id + "_"+
					// treeLevel,
					type : childObj.type,
					children : []
				})
			}
		}
	}
	var root = {};
	if (oldTreeRootNode && treeSelectedNode) {
		root = oldTreeRootNode;
		var id = treeSelectedNode.id;

		treeSelectedNode = null;
		root2 = findNodeInTree(id, oldTreeRootNode);
		if (!root2)
			return;
		if (root2 && root2.children)
			return;
		root2.children = [];
		var key2;
		for ( var key in distinctParentNodes) {
			for (var i = 0; i < distinctParentNodes[key].children.length; i++) {// on
				// ajoute
				// les
				// nouveaux
				// noeuds
				// à
				// l'ancien
				// arbre
				var newChild = distinctParentNodes[key].children[i];
				var p = newChild._id.indexOf("_");

				var newId = newChild._id.substring(0, p);
				var oldChild = findNodeInTree(newId, oldTreeRootNode);
				if (oldChild) {// si le noeud existe déjà on ne l'ajoute pas
					;// console.log(JSON.stringify(oldChild));
					continue;
				}

				root2.children.push(newChild);
			}
		}

		root = oldTreeRootNode;

	} else {
		/*
		 * var root = {
		 * 
		 * id : -1, isRoot : true, name : "..", // name : "-" + currentVariable,
		 * children : [] } for ( var key in distinctParentNodes) {
		 * 
		 * root.children.push(distinctParentNodes[key]); }
		 */
		for ( var key in distinctParentNodes) {
			var root = distinctParentNodes[key];
		}
	}

	oldTreeRootNode = root;
	treeLevel += 1;
	if (!d3tree)
		d3tree = new D3Tree2($("#graphDiv"));
	d3tree.drawTree(root);
}

//}


