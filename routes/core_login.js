var express=require('express');
var router=express.Router();
var dotenv = require('dotenv');
dotenv.config();

// MySQL Connection 
var mysql=require('mysql');
const connection = mysql.createConnection({
	host: '128.199.23.207',
	user: "csi",
	password: "csi",
	database: 'csiApp2022'
});

connection.connect(function(err) {
	if (!err){
        	console.log('MySql Connected Sucessfully! This is Login Page');
    	}
    	else{
        	console.log('MySql Not Connected Sucessfully! This is Login Page');
    	}
});

router.post('/',(req,res)=>{
	//uid stored as id from app, will be given the core_id
     	var id=req.body.id;
     	var password=req.body.password;

	//Query to select the tuple of the user
     	connection.query('SELECT * FROM core_details WHERE core_id = ?',[id],function(error,result){
			console.log(result[0])
     		if(error){
      			//console.log("Error");
      			res.status(404);
     		}
		else{	
       			if(result.length>0){
          			if(result[0].core_pwd==password){
						//nested sql query to take role name from role_master table
						connection.query('Select role_name from core_role_master CR where EXISTS ( select core_role_id from core_details CD where CR.role_id=CD.core_role_id and CD.core_id=?)',[id],function(error2,result2){
							res.status(200).send({
							"role":result2[0].role_name,
							 "name":result[0].core_en_fname,
							 "dp":result[0].core_profilepic_url
						   });
						})
					//console.log("Succesfully Logged In");
             				
          			}
	        		else{
            				//Users password do not match
					console.log("Password Does Not Match");
          				res.sendStatus(400);
          			}
       			}
       			else{
         			//User does not exist
				console.log("User does not exist");
         			res.sendStatus(400);
       			}
      		}
    	});
});

module.exports = router;