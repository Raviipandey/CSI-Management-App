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

	connection.query('SELECT e.proposals_event_name, e.proposals_event_category,tech_comment, e.proposals_event_date, e.speaker, e.proposals_venue, e.proposals_reg_fee_csi, e.proposals_reg_fee_noncsi, e.proposals_prize, e.proposals_desc,e.proposals_creative_budget,e.proposals_publicity_budget,e.proposals_guest_budget,t.qs_set, t.internet, t.tech_comment, t.software_install FROM core_proposals_manager e inner join core_technical_manager t on e.cpm_id=t.cpm_id WHERE t.cpm_id=?',[eid], function (error, results) {
		if (error){
			console.log("Failed To view Technical events");
			res.sendStatus(400);
		}
		else
		{
			console.log(eid);	
			console.log(results);	
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


	connection.query('UPDATE core_technical_manager SET qs_set=?, internet=?, software_install=?,tech_comment=?,tech_req_status=3 WHERE cpm_id=?',[qs_set, internet,software_install,comment,eid],function(err, result){
		if(err){
				console.log("Failed to update Technical Event ");			
				res.sendStatus(400);
			}
		else{
					console.log("Sucessfully updated technical event");
					console.log(result);
					res.sendStatus(200);
			}
		});
});
	
module.exports = router;