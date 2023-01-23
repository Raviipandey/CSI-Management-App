var express = require('express');
var router = express.Router();
var mysql = require('mysql');


// MySQL Connection
var mysql = require('mysql');
var connection = mysql.createConnection({
    host: '128.199.23.207',
	user: "csi",
	password: "csi",
	database: 'csiApp2022'
});
connection.connect(function(err) {
    if (!err) {
       console.log('Connected to MySql!Technical.js');
    } else {
        console.log('Not Connected to MySql!Technical.js');
    }
});

//Viewing Events
router.post('/viewEvents', (req, res) =>{
	var eid = req.body.eid;

	connection.query('SELECT e.proposals_event_name, e.proposals_event_category, e.proposals_event_date, e.speaker, e.proposals_venue, e.proposals_reg_fee_csi, e.proposals_reg_fee_noncsi, e.proposals_prize, e.proposals_desc,e.creative_budget,e.publicity_budget,e.guest_budget,t.qs_set, t.internet, t.comment, t.software_install FROM events e inner join technical t on e.eid=t.eid WHERE t.eid=?',[eid], function (error, results) {
		if (error){
			console.log("Failed To view Technical events");
			res.sendStatus(400);
		}
		else
		{	
			console.log("Sucessfully viewed Technical events");
			res.status(200).send(results[0]);	
		}
	});
});

//Edit Technical
router.post('/editEvents',(req,res)=>{
	var eid = req.body.eid;
	var qs_set = req.body.qs_set;	
	var internet = req.body.internet;	
	var comment = req.body.comment;
	var software_install = req.body.software_install;	


	connection.query('UPDATE technical SET qs_set=?, internet=?, software_install=?,comment=?,status=3 WHERE eid=?',[qs_set, internet,software_install,comment,eid],function(err){
		if(err){
				console.log("Failed to update Technical Event ");			
				res.sendStatus(400);
			}
		else{
					console.log("Sucessfully updated technical event");
					res.sendStatus(200);
			}
		});
});
	
module.exports = router;