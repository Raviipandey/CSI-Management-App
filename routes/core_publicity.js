var express = require('express');
var router = express.Router();

// MySQL Connection
var mysql=require('mysql');
var connection=mysql.createConnection({
	host: '128.199.23.207',
	user: "csi",
	password: "csi",
	database: 'csiApp2022'
});
connection.connect(function(err){
	if(err){
		console.log('Not Connected to core Publicity');
	}
	else{
		console.log("Connected To core Publicity");
	}
});

//Viewing Events
router.post('/viewEvent', (req, res) =>{
    var eid = req.body.eid;

	const sql = "SELECT e.proposals_event_name, e.proposals_event_category, e.proposals_event_date, e.speaker, e.proposals_venue, e.proposals_reg_fee_csi, e.proposals_reg_fee_noncsi, e.proposals_prize, e.proposals_desc , e.proposals_creative_budget, e.proposals_publicity_budget, e.proposals_guest_budget, p.pr_desk_publicity, p.pr_class_publicity , p.pr_member_count, p.pr_comment, p.pr_rcd_amount , p.pr_spent, (SELECT GROUP_CONCAT(pt.pub_tasks) FROM publicity_tasks pt WHERE pt.cpm_id = p.cpm_id) AS tasks FROM core_proposals_manager e inner join core_pr_manager p on e.cpm_id=p.cpm_id WHERE p.cpm_id=?;	"
    connection.query(sql, [eid], function (error, results) {
        if (error){
            console.log("Failed To view Publicity events");
            res.sendStatus(400);
        }
        else
        {
            console.log("Successfully viewed Publicity events");
			console.log(results);
			try {
				const tasksArray = results[0].tasks.split(','); // split the comma-separated string into an array
            	results[0].tasks = tasksArray; // replace the tasks property with the array
				console.log(tasksArray);
			} catch (error) {
				TypeError
			}
            res.status(200).send(results[0]);
        }
    });
});
//Adding checkboxes

router.post('/addcheckbox',(req,res)=>{
    var eid = req.body.eid;
    console.log(eid);   
    var checkedCheckboxes = req.body.checkedCheckboxes;
    lenghtofarray = Object.keys(checkedCheckboxes || {}).length
    console.log(lenghtofarray);
    // Store the checkedCheckboxes list in the MySQL database
    for (let i = 0; i < lenghtofarray; i++) {
        
        const checked = 1;
        const checkbox = checkedCheckboxes[i];

        // Check if the checkbox is already present in the table
        connection.query("SELECT * FROM publicity_tasks WHERE pub_tasks=? AND cpm_id=?", [checkbox, eid], (error, results) => {
            if (error) {
                console.error("Error aa raha hai" , error);
                res.sendStatus(500);
                return;
            } else {
                if (results.length > 0) {
                    console.log("Checkbox already present");
                    return;
                } else {
                    connection.query("INSERT INTO publicity_tasks (pub_tasks, status , cpm_id) VALUES (?, ? , ?)", [checkbox, checked , eid], (error, results) => {
                        if (error) {
                            console.error("Error aa raha hai" , error);
                            res.sendStatus(500);
                            return;
                        } else {
                            console.log("Ho gai entryy" ,results);
                        }
                    });
                }
            }
        });
    }
});

/*{
	"eid":"1WMpK"
}*/

/*res
{
    "name": "GOC",
    "theme": "coding",
    "event_date": "2019-09-24T18:30:00.000Z",
    "speaker": "Venkat Raman",
    "venue": "IT LAB-2",
    "reg_fee_c": 50,
    "reg_fee_nc": null,
    "prize": 1200,
    "description": "Hello World",
    "creative_budget": 50,
    "publicity_budget": 50,
    "guest_budget": 25,
    "desk": 1,
    "in_class": 0,
    "target": "50",
    "comment": "hello",
    "collected": 100,
    "spent": 100
}*/

//Edit form
router.post('/editPublicity',(req,res)=>{
	var pr_rcd_amount=req.body.pr_rcd_amount;
	var pr_desk_publicity=req.body.pr_desk_publicity;
	var pr_class_publicity=req.body.pr_class_publicity;
	var pr_member_count=req.body.pr_member_count;
	var pr_comment=req.body.pr_comment;
	var eid=req.body.eid;
	var pr_spent=req.body.pr_spent;

	//pushing into publicty table 
	connection.query('UPDATE core_pr_manager SET pr_desk_publicity=?, pr_class_publicity=?,pr_member_count=?,pr_comment=?, pr_rcd_amount=?,pr_spent =? ,pr_status=3 WHERE cpm_id=?',[pr_desk_publicity,pr_class_publicity,pr_member_count,pr_comment,pr_rcd_amount,pr_spent,eid],function(error){
	if (error){
			console.log(error);
			console.log("Failed to edit publicity event");
			res.sendStatus(400);
		}
	else
	{
		console.log("Sucessfully updated publicty event");
		res.sendStatus(200);
	}
	});
});
/*{
"eid":"1WMpK",
"desk":"1",
"in_class":"0", 
"target":"30", 
"comment":"Need more details",
"collected":"200",
"spent":"100"
}*/
/*
router.post('/req',(req,res)=>{
	var eid=req.body.eid;
	var desk=req.body.desk;
	var in_class=req.body.in_class;
	var target=req.body.target;
	var comment=req.body.comment;
	var collected=req.body.collected;
	var spent=req.body.spent;

	//pushing into publicty table 
	connection.query('INSERT INTO publicity(eid,desk,in_class,target,comment,collected,spent) VALUES(?,?,?,?,?,?,?)',[eid,desk,in_class,target,comment],function(error,results,fields){
		if(error){	
			console.log(error);		
			res.sendStatus(400);
		}
		else{
			res.sendStatus(200);
		}
	});
});*/

/*{
	"eid":"TYgBt","desk":"1","in_class":"0", "target":"30", "comment":"Need more details"
}*/


// router.post('/addMember',(req,res)=>{
// 	var name = req.body.name;
// 	var eid = req.body.eid;
// 	var id = req.body.id;
// 	var work = req.body.work;
// 	connection.query('INSERT INTO Members values(?,?,?,?)',[name, id, work, eid],function(error,results,fields){
// 	if (error){
// 		console.log(error);
// 		res.sendStatus(400);
// 	}
// 	else{
// 		console.log("Member Added")
// 		res.sendStatus(200)
// 	}
// 	});
// 	});	
/*{
	"id":"2017134980","name":"Mushira","work":"Install python, npm etc in the system", "eid":"6"

}*/

// router.post('/viewMember',(req,res)=>{
// 	var id = req.body.id;
// connection.query('SELECT name, id, CONVERT(work USING utf8) as work FROM Members WHERE id=?',[id],function(error,results,fields){
// 	if (error){
// 		console.log(error);
// 		res.sendStatus(400);
// 	}
// 	else{
// 		res.status(200).send(results);
// 	}
// 	});
// 	});

// /*{
// 	"id":"2017134980"

// }*/

// router.post('/deleteMember',(req,res)=>{
// 	//var id = req.body.id;
// 	for (var i = 0; i < req.body.members.length; i++)
// {
// 	var id = req.body.members[i]
// 		connection.query('delete FROM Members WHERE id=?',[id],function(error,results,fields){
// 	if (error){
// 		console.log(error);
// 		res.sendStatus(400);
// 	}
// 	else{
// 		console.log("Member deleted")
// 	}
// 	});
// }
// res.sendStatus(200)
// });

// /*{
// 	"members":[
//       "2017134980",
//       "2017134956"
//    ]
// }*/
		
// router.post('/editMember',(req,res)=>{
// 	var name = req.body.name;
// 	var id = req.body.id;
// 	var work = req.body.work;
// 	connection.query('UPDATE Members set name=?, work=? WHERE id=?',[name, work, id],function(error,results,fields){
// 	if (error){
// 		console.log(error);
// 		res.sendStatus(400);
// 	}
// 	else{
// 		console.log("Member Edited!")
// 		res.sendStatus(200)
// 	}
// 	});
// 	});

// {
// 	"id":"2017134980","name":"Mushira","work":"Install python, npm etc in the system"

// }

// router.get('/ListMembers', (req, res) =>{
// 	//fetching from events table
// 	connection.query('SELECT id, name, CONVERT(work USING utf8) as work FROM Members', function (error, results, fields) {
// 	if (error){
// 		console.log(error)
// 		res.sendStatus(400);
// 	}
// 	else
// 		res.status(200).send(results);	
// 	});
// });

// router.post('/insertBudget', (req, res) =>{
// 	var eid = req.body.eid;
// 	var collected = req.body.collected;
// 	var refunded = req.body.refunded;
// 	var spent = req.body.spent;
// 	var balance = req.body.balance;
// 	connection.query('INSERT INTO budget values(?,?,?,?,?)', [eid, collected, refunded, spent, balance], function (error, results, fields) {
// 	if (error){
// 		console.log(error)
// 		res.sendStatus(400);
// 	}
// 	else
// 	{
// 		console.log("Budget Inserted")
// 		res.sendStatus(200);	
// 	}
// 	});
// });
// /*{
// 	"eid":"6","collected":"2000","refunded":"500", "spent":"800", "balance":"700"
// }*/


// router.post('/viewBudget',(req,res)=>{
// 	var eid = req.body.eid;
// connection.query('SELECT * FROM budget WHERE eid=?',[eid],function(error,results,fields){
// 	if (error){
// 		console.log(error);
// 		res.sendStatus(400);
// 	}
// 	else{
// 		res.status(200).send(results);
// 	}
// 	});
// 	});
// /*{
// 	"eid":"2017134980"

// }*/


// //will appear along with pr details
// router.post('/Venue',(req,res)=>{
// 	var eid = req.body.eid;
// 	var v_ready=req.body.v_ready
// 	var v_comm=req.body.v_comm
// 	//venue_ready will hve chckbox
// connection.query('UPDATE events set venue_status=?, venue_comment=? WHERE eid=?',[eid],function(error,results,fields){
// 	if (error){
// 		console.log(error);
// 		res.sendStatus(400);
// 	}
// 	else{
// 		res.sendStatus(200);
// 	}
// 	});
// 	});
// /*{
// 	"eid":"6","v_ready":"0","v_comm":"Needs chairs"
// }*/

//Fetching added checkboxes

router.get('/checkboxes', function(req, res) {
    const status = 1;
    const sql = 'SELECT task FROM `publicity_tasks` WHERE status = 1';
    connection.query(sql, [status], function(error, results, fields) {
        if (error) {
            console.error(error);
            res.status(500).send('Error retrieving checkboxes');
        } else {
			console.log(results);
            const checkboxes = results.map(result => result.name);
            res.json(checkboxes);
        }
    });
});

module.exports = router;
