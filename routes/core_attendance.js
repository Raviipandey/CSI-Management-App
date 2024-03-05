var express=require('express');
var router=express.Router();
var dotenv = require('dotenv');
dotenv.config();

// MySQL Connection
var mysql=require('mysql');
const connection=mysql.createConnection({
	host: '128.199.23.207',
    user: "csi",
    password: "csi",
    database: 'csiApp2022'
});

connection.connect(function(err){
    	if(!err){
        	console.log('Connected to MySql! core Attendance');
    	}
	else{	
        	console.log("Not Connected To Mysql! Attendance");
    	}
});

//Attendance Request
router.post('/request',(req,res)=>{
    var ad_id=req.body.ad_id;
	var id=req.body.id; //core_id
	var date=req.body.date;
	var s1 = req.body.s1;
	var s2 = req.body.s2;
	var s3 = req.body.s3;
	var s4 = req.body.s4;
	var s5 = req.body.s5;
	var s6 = req.body.s6;
	var s7 = req.body.s7;
//	var timeslot=req.body.timeslot;
	var sublecsmissed=req.body.sublecsmissed;
	var reason=req.body.reason;

	//fetching name from users table
	connection.query('SELECT core_en_fname,core_class FROM core_details WHERE core_details.core_id=?',[id],function(err,rest){
		console.log(rest)
		if (err){
			console.log(err);
			res.sendStatus(400);
		}
		else{
			//pushing into request(attendance_details) table
			//INSERT INTO attendance_details (core_id,core_date,s1,s2,s3,s4,s5,s6,s7,core_timeslot,core_lecsmissed_sub,core_reason) VALUES(5,'2023-10-19',1,0,0,0,0,0,1,'11:00:00',"BI","test s");
			connection.query('INSERT INTO attendance_details(core_id,core_date,s1,s2,s3,s4,s5,s6,s7,core_lecsmissed_sub,core_reason) VALUES(?,?,?,?,?,?,?,?,?,?,?)',[id,date,s1,s2,s3,s4,s5,s6,s7,sublecsmissed,reason],function(err,results,fields){
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

//Display all the requests
router.post('/requestlist',(req,res)=>{
	connection.query('SELECT * FROM attendance_details where status = "WAITING"',function(error,result){
		if(error){
			//console.log"(Error");
			res.sendStatus(400);
		}
		else
		{
    			res.status(200).send(result);
		}
	});
});

//Accept json array,move the record from request to finallist
router.post('/finallist', (req, res) =>{
	var ids=req.body.accepted;

	for(i in ids){ //index value=i
		//connection.query('DELETE FROM attendance_details WHERE ad_id=?',[ids[i]],function (error,result){
			connection.query('UPDATE attendance_details SET status = "ACCEPTED" WHERE ad_id=?',[ids[i]],function (error,result){
			if (error){
				//console.log("Error");
				res.sendStatus(400);
			}
		});
	}
	res.sendStatus(200);


  	//for(var i=0;i<req.body.accepted.length;i++)
	// {
	// 	var rid = req.body.accepted[i]
	// 	connection.query('INSERT INTO attendance_details_finallist SELECT * FROM attendance_details WHERE ad_id = ?',[rid],  function (error, fields){ //rid=ad_id
	// 		if (error){
	// 			//console.log(rid);
	// 			res.sendStatus(400);
	// 		}
	// 		else{
	// 			connection.query('DELETE FROM attendance_details WHERE ad_id = ?',[rid], function (error, results, fields) {
	// 				if (error){
	// 					//console.log("Error");
	// 				res.sendStatus(400);
	// 				}
	// 				else{
	// 					//console.log("Deleted succesfully");
	//     					res.sendStatus(200);
	// 				}
	// 			});
	// 			//console.log("Inserted succesfully");
	// 		}
	// 	});
	// }
});

//Attendance Reject
router.post('/reject',(req,res)=>
{
	var ids=req.body.rejected;

	//Deleting from request table
	for(i in ids){ //index value=i
		//connection.query('DELETE FROM attendance_details WHERE ad_id=?',[ids[i]],function (error,result){
			connection.query('UPDATE attendance_details SET status = "REJECTED" WHERE ad_id=?',[ids[i]],function (error,result){
			if (error){
				//console.log("Error");
				res.sendStatus(400);
			}
		});
	}
	res.sendStatus(200);
});

//SBC Attendance
// router.post('/view',(req,res)=>{
// 	var id=req.body.id; 
// 	var year=req.body.year;
// 	var name=req.body.name;
	
	// connection.query('SELECT core_en_fname, core_class FROM core_details WHERE core_details.core_id=?',[id],function(error,rest2){
	// 	if(error){
	// 		res.sendStatus(400);
	// 		console.log(error);
	// 	}
	// 	else{
			//connection.query('SELECT sum(s1+s2+s3+s4+s5+s6+s7) as total FROM attendance_details_finallist WHERE adf_id=?',[adf_id], function (error,result){
			//connection.query('SELECT sum(s1+s2+s3+s4+s5+s6+s7) as total FROM attendance_details WHERE status = "ACCEPTED"', function (error,result){
			// connection.query('SELECT cd.core_en_fname,cd.core_class,sum(ad.s1+ad.s2+ad.s3+ad.s4+ad.s5+ad.s6+ad.s7) as hours_spent from core_details cd inner join attendance_details ad on cd.core_id=ad.core_id AND status= "ACCEPTED" group by cd.core_id;',function (error , result){
			// 	if(error){
			// 		res.sendStatus(400);
			// 		console.log(error);
			// 	}
			// 	else{
			// 		res.status(200).send(result);
			// 		console.log("Successfull");
			// 	}
			// });
				
				//SELECT sum(s1+s2+s3+s4+s5+s6+s7) as total FROM attendance_details WHERE status = "ACCEPTED"', function (error,result){	
			
			//});
	//	}
	//});
// });

//SBC Attendance
router.post('/view',(req,res)=>{
	var id=req.body.id; 
	var year=req.body.year;
	var name=req.body.name;
	var hours_spent=req.body.hours_spent;

connection.query('SELECT cd.core_en_fname as Name,cd.core_class as Class ,sum(ad.s1+ad.s2+ad.s3+ad.s4+ad.s5+ad.s6+ad.s7) as hours_spent from core_details cd inner join attendance_details ad on cd.core_id=ad.core_id AND status="ACCEPTED"  where cd.core_class = ? group by cd.core_id',[year],function(error,result){
	//connection.query('SELECT cd.core_en_fname as Name,cd.core_class ,sum(ad.s1+ad.s2+ad.s3+ad.s4+ad.s5+ad.s6+ad.s7) as hours_spent from core_details cd inner join attendance_details ad on cd.core_id=ad.core_id AND status="ACCEPTED" group by cd.core_id',function(error,result){
		if(error){
			res.sendStatus(400);
			console.log(error);
		}
		else{
			res.status(200).send(result);
			console.log("Successfull");
		}
	})
});



module.exports = router;



