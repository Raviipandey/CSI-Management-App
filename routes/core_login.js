var express = require('express');
var router = express.Router();
var dotenv = require('dotenv');
dotenv.config();

// Firebase Admin SDK setup
var admin = require('firebase-admin');
var serviceAccount = require('../firebase/ServiceAccount.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

// MySQL Connection
var mysql = require('mysql');
const connection = mysql.createConnection({
  host: '128.199.23.207',
  user: "csi",
  password: "csi",
  database: 'csiApp2022'
});

connection.connect(function (err) {
  if (!err) {
    console.log('MySql Connected Successfully! This is Login Page');
  } else {
    console.log('MySql Not Connected Successfully! This is Login Page');
  }
});

router.post('/', (req, res) => {
    var mobno = req.body.mobno;
    var password = req.body.password;
    var fcmtoken = req.body.fcmtoken;
    console.log(mobno, password, fcmtoken);
    
    // Query to update the fcm_token in core_details table
    connection.query('UPDATE core_details SET fcm_token = ? WHERE core_mobileno = ?', [fcmtoken, mobno], (err, result) => {
        if (err) {
            console.log("Error updating fcm_token in core_details table:", err);
            res.sendStatus(500);
            return;
        }
        
        // Query to fetch user details from core_details table
        connection.query('SELECT * FROM core_details WHERE core_mobileno = ?', [mobno], (error, result) => {
            if (error) {
                console.log("Error fetching user details from MySQL:", error);
                res.sendStatus(500);
                return;
            }

            if (result.length > 0) {
                var user = result[0];
                // Query to fetch role_name from core_role_master table based on role_id
                connection.query('SELECT role_name FROM core_role_master WHERE role_id = ?', [user.core_role_id], (roleErr, roleResult) => {
                    if (roleErr) {
                        console.log("Error fetching role_name from core_role_master:", roleErr);
                        res.sendStatus(500);
                        return;
                    }

                    var roleName = (roleResult.length > 0) ? roleResult[0].role_name : null;

                    // Authenticate email and password using Firebase Admin SDK
                    admin.auth().getUserByEmail(user.core_email)
                        .then((userRecord) => {
                            // User exists in Firebase Authentication
                            // Check if passwords match
                            if (user.core_pwd === password) {
                                res.status(200).send({
                                    "role": roleName,
                                    "name": user.core_en_fname,
                                    "dp": user.core_profilepic_url,
                                    "fcmtoken": user.fcm_token,
                                    "userid": user.core_id
                                });
                            } else {
                                console.log("Password does not match");
                                res.sendStatus(401); // Unauthorized
                            }
                        })
                        .catch((error) => {
                            // Handle errors
                            console.log("Error:", error);
                            res.sendStatus(500);
                        });
                });
            } else {
                // User not found in MySQL database
                console.log("User does not exist");
                res.sendStatus(404);
            }
        });
    });
});

router.post('/resetpassword', (req, res) => {
	var email = req.body.email;
	var newPassword = req.body.newPassword;
	console.log(email , newPassword);
  
	admin.auth().getUserByEmail(email)
	  .then((userRecord) => {
		// User exists in Firebase Authentication
		// Update password in Firebase Authentication
		return admin.auth().updateUser(userRecord.uid, {
		  password: newPassword
		});
	  })
	  .then(() => {
		// Password updated successfully in Firebase Authentication
		// Now, update password in MySQL database
		connection.query('UPDATE core_details SET core_pwd = ? WHERE core_email = ?', [newPassword, email], (error, result) => {
		  if (error) {
			console.log("Error updating password in MySQL:", error);
			res.sendStatus(500);
		  } else {
			console.log("Password updated successfully in MySQL");
			res.sendStatus(200);
		  }
		});
	  })
	  .catch((error) => {
		// Handle errors
		console.log("Error resetting password:", error);
		res.sendStatus(500);
	  });
  });
module.exports = router;