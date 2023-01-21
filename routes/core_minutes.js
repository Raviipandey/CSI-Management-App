var express=require('express');
var router=express.Router();
var dotenv = require('dotenv');
dotenv.config();


// MySQL Connection
var mysql=require('mysql');
var connection=mysql.createConnection({
	host: '128.199.23.207',
	user: "csi",
	password: "csi",
	database: 'csiApp2022'
});
connection.connect(function(err) {
    	if (!err){
        	console.log('Connected to MySql!Minutes less goo');
    	}
	else{
        	console.log("Not Connected To Mysql! Minutes");
    	}
});

//To Create Minute
router.post('/create',(req,res)=>{
	var id=req.body.id;
	var agenda = req.body.agenda;
	console.log(agenda);
	// var s_time = req.query.s_time;
	// var e_time = req.query.e_time;
	var points = req.body.points;
	var work=JSON.stringify(req.body.work);
	var absentee = req.body.absentee;

	//fetching creator from users table
	// connection.query('SELECT core_en_fname FROM core_details where core_id=?',[id],function(error,creator){
	// 	if(error){
	// 		//console.log("Error");
	// 		res.sendStatus(400);
	// 	}
	// 	else{
	// 		//pushing into minute table 
	// 		connection.query('INSERT INTO core_minute_manager(cmm_id,minute_date,minute_starttime,minute_endtime,minute_objective,minute_details) VALUES (?,?,?,?,?,?);',[id, date , s_time , e_time , obj , details],function(err,result){
	// 			if(err){
	// 				console.log(err);
	// 				res.sendStatus(400);
	// 			}
	// 			else{
	// 				//console.log("Data Inserted");
	// 				res.sendStatus(200);
	// 			}
	// 		});
	// 	}
	// });

	//demoooo

	//fetching creator from users table
	connection.query('SELECT core_en_fname FROM core_details where core_id=?',[id],function(error,creator){
		if(error){
			//console.log("Error");
			res.sendStatus(400);
		}
		else{
			//pushing into minute table 
			connection.query('INSERT INTO core_minute_manager(minute_by_core_id, minute_objective,minute_details,core_ab_mem_name,minute_work,creator) VALUES (?, ?, ?, ?, ?, ?);',[id, agenda, points, absentee, work, creator[0].core_en_fname],function(err,result){
				if(err){
					console.log(err);
					res.sendStatus(400);
				}
				else{
					//console.log("Data Inserted");
					res.sendStatus(200);
				}
				
			});
		}
	});
});

//To List All minutes
router.post('/list', (req, res) =>{
	var id = req.body.id;

	//fetching from minute table
	connection.query('SELECT minute_by_core_id,minute_objective,minute_date,minute_time,creator, minute_details,core_ab_mem_name, CONVERT(minute_work USING utf8) as minute_work FROM core_minute_manager order by minute_date desc',function(error,result){
		if (error){
			//console.log("Error");
			res.sendStatus(400);
		}
		else{
			//console.log("Succesfully Listed");
			res.status(200).send(result);
		}
	});
});

router.post('/viewminute',(req,res)=>{
	var date = req.body.date;
	var time = req.body.time;

	//fetching from minute table
	connection.query('SELECT minute_by_core_id,minute_objective,minute_date,minute_time,creator, CONVERT(minute_details USING utf8) as minute_details, core_ab_mem_name, CONVERT(minute_work USING utf8) as minute_work FROM core_minute_manager WHERE minute_date=? and minute_time=?;',[date,time],function(error,result){
		if (error){
			//console.log("Error");
			res.sendStatus(400);
		}
		else{
			//console.log("Succesfully Listed");
			res.status(200).send(result[0]);
		}
	});
});	

router.get('/members', (req, res) => {
    // var date = req.body.date;

    connection.query('SELECT core_en_fname FROM core_details where(core_en_fname!="Nilesh" && core_en_fname!="Prasad")', function(error, results) {
        if (error) {
            console.log("Fail to view core member names");
            res.sendStatus(400);
        } else {
            for (var i = 0; i < results.length; i++) {
                results[i] = results[i].core_en_fname;
            }
            console.log("Successfully viewed core member names");
            res.status(200).send({ "members": results });
        }
    });
});

module.exports = router;
