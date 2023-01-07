var express = require('express');
var router = express.Router();
var dotenv = require('dotenv');
dotenv.config();
var generator = require('generate-password');
var nodemailer = require('nodemailer');

var transporter = nodemailer.createTransport({
  service: 'gmail',
  auth: {
    user: process.env.email_id,
    pass: process.env.email_password
  }
});

// MySQL Connection
var mysql = require('mysql');
const { json } = require('body-parser');
var connection = mysql.createConnection({
  host: '128.199.23.207',
	user: "csi",
	password: "csi",
	database: 'csiApp2022'
});
connection.connect(function(err) {
    if (!err) {
      console.log('Connected to MySql!Profileeee');
    } else {
      console.log('Not Connected To Mysql!Profile');
    }
});

router.get('/',(req,res)=>{
	var id = req.query.id;
  // console.log(id);
	//fetching deatails from profile table
	connection.query('SELECT core_id,core_en_fname,core_role_id,core_email,core_mobileno,core_branch,core_rollno,core_class FROM core_details where core_id=?',[id],function(error,results,fields){
    connection.query('Select role_name from core_role_master CR where EXISTS ( select core_role_id from core_details CD where CR.role_id=CD.core_role_id and CD.core_id=?)',[id],function(error2,result2){
      
      // console.log(results)
      if(error)
      {
        console.log(error);
        res.sendStatus(400);
      }
      else
      var id = req.query.id;
      connection.query('SELECT TIMESTAMPDIFF(year, membership_start_date, membership_end_date) AS Membership_left from core_membership where core_id=?',[id],function(error,result3,fields){
        // res.send(results[0]);
        res.status(200).send({
          "name":result2[0].role_name,
          "core_id":results[0].core_id,
          "core_en_fname":results[0].core_en_fname,
          "core_role_id":results[0].core_role_id,
          "core_email":results[0].core_email,
          "core_mobileno":results[0].core_mobileno,
          "core_branch":results[0].core_branch,
          "core_rollno":results[0].core_rollno,
          "core_class":results[0].core_class,
          "membership_left":result3[0].Membership_left
           });
        // res.send(result2[0])
        // console.log(results[0]);
        // console.log(result2[0]);
        })

    })
    

	});
});

router.post('/new',(req,res) => {
  var password = generator.generate({
    length: 10,
    numbers: true
  });
  var id = req.body.studentId;
  
  var name = req.body.fullName;
  var email = req.body.email;
  var phone = req.body.phone;
  var year = req.body.yearSelect;
  var branch = req.body.branchSelect;
  var rollno = req.body.rollno;
  var batch = req.body.batchSelect;
  var years = req.body.membershipSelect;
  var mailOptions = {
    from: process.env.email_id,
    to: email,
    subject: 'First Time Login Password',
    text: 'Your password for First Time Login into CSI-Management App is '+password
  };
  transporter.sendMail(mailOptions, function(error, info){
      if (error) {
        console.log(error);
      } else {
        console.log('Email sent: ' + info.response);
      }
    });
  connection.query('Insert into profile values(?,?,?,?,?,?,?,?,?,?,?)',[id,password,"member",name,email,phone,year,branch,rollno,batch,years],function(error,results,fields){
    if(error){
      console.log(error);
      res.sendStatus(400);
    }
    else{
      res.sendStatus(200);
    }
  });
});

router.post('/view', (req,res) => {
  connection.query('Select id,name,email,phone,year,branch,rollno,batch,membership_left from profile where role=\'member\'',[],function(error,results,fields){
    if(error){
      console.log(error);
      res.sendStatus(400);
    }
    else{
      res.send(results);
    }
  });
});

router.post('/edit',(req,res)=>{
  console.log(req.body);
	var id = req.query.id;
	var name = req.query.name;
	var year = req.query.year;
	var rollno = req.query.rollno;


	//fetching creator from users table
	connection.query('UPDATE core_details SET core_en_fname =?, core_class =?, core_rollno =? WHERE core_id=?',[name,year,rollno,id],function(error,result,fields){
  // console.log()
	if (error)
	res.sendStatus(400);
	else
	{
		res.sendStatus(200);
		console.log("Data Updated");
    console.log(result)
	}
	});
});


module.exports = router;
