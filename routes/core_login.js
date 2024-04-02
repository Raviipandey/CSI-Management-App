var express = require('express');
var router = express.Router();
var dotenv = require('dotenv');
dotenv.config();
const app = express();
const path = require('path');
const crypto = require('crypto');
const { server_url} = require('../serverconfig');
const fs = require('fs');
// Firebase Admin SDK setup
var admin = require('firebase-admin');
var serviceAccount = require('../firebase/ServiceAccount.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});



function generateSessionToken() {
    return crypto.randomBytes(48).toString('hex'); // Generates a secure, random 48-byte hex string
}

// MySQL Connection
var mysql = require('mysql');
const connection = mysql.createConnection({
  host: '128.199.23.207',
  user: "csi",
  password: "csi",
  database: 'csiApp2022'
});

app.use("/views",express.static("views"));
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'ejs');

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

    const newSessionToken = generateSessionToken(); 
    
    // Query to update the fcm_token in core_details table
    connection.query('UPDATE core_details SET session_token = ?, fcm_token = ? WHERE core_mobileno = ?', [newSessionToken, fcmtoken, mobno], (err, result) => {
        if (err) {
            console.log("Error updating fcm_token and udid in core_details table:", err);
            return res.status(500).json({ error: 'Database error during token update' });
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
                                    "userid": user.core_id, 
                                    "newSessionToken":user.session_token
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

var userFirstName = "";

router.post('/resetpassword', (req, res) => {
    const email = req.body.email;
    console.log(email);

    // Check if the email exists in the core_details table
    connection.query('SELECT core_email, core_en_fname FROM core_details WHERE core_email = ?', [email], (error, results) => {
        if (error) {
            console.log("Error checking email existence in the database:", error);
            res.sendStatus(500);
            return;
        }

        // If no results found, email does not exist
        if (results.length === 0) {
            console.log("Email does not exist in the database.");
            res.status(404).send("Email not found.");
            return;
        }

        // Extract the user's first name from the query results
        userFirstName = results[0].core_en_fname;

        // Generate a unique token for the magic link with expiration time
        const resetPasswordToken = generateResetPasswordToken();

        // Store the token and its expiration time in the pass_reset_token column of core_details table
        connection.query('UPDATE core_details SET pass_reset_token = ?, pass_reset_token_expiration = ? WHERE core_email = ?', 
            [resetPasswordToken.token, resetPasswordToken.expirationTime, email], (error, result) => {
                if (error) {
                    console.log("Error updating reset password token in the database:", error);
                    res.sendStatus(500);
                    return;
                }

                // Send the magic link to the user via email
                const magicLink = `https://csiapp.dbit.in/app/${resetPasswordToken.token}`;
                sendMagicLinkEmail(email, magicLink, userFirstName);

                res.status(200).send("Magic link sent successfully!");
        });
    });
});


function generateResetPasswordToken() {
    // Generate a random token using a library like crypto or uuid
    const token = Math.random().toString(36).substr(2); // This generates a random alphanumeric token

    // Set expiration time (3 minutes from now)
    const expirationTime = new Date().getTime() + (3 * 60 * 1000); // 3 minutes in milliseconds

    return { token, expirationTime };
}

// Read the image files
const imagePath1 = path.join(__dirname, '../', 'server_uploads', 'csiheader.png');
const imagePath2 = path.join(__dirname, '../', 'server_uploads', 'resetpass.png');

function sendMagicLinkEmail(email, magicLink) {
    // Implement code to send an email containing the magic link
    // For example, using nodemailer:
    const nodemailer = require('nodemailer');
  
    fs.readFile(imagePath1, { encoding: 'base64' }, (err1, imageData1) => {
        fs.readFile(imagePath2, { encoding: 'base64' }, (err2, imageData2) => {
            if (err1 || err2) {
                console.error("Error reading image files:", err1 || err2);
                return;
            }
    
            // Create nodemailer transporter
            const transporter = nodemailer.createTransport({
                service: 'gmail',
                auth: {
                    user: 'csi.dbit.management@gmail.com',
                    pass: 'dwsc qgvo exts fnko'
                }
            });
    
            // Attach images to mailOptions using cid
            const mailOptions = {
                from: 'csi.dbit.management@gmail.com',
                to: email,
                subject: 'Reset Your Password',
                html: `
                <!DOCTYPE HTML PUBLIC "-//W3C//DTD XHTML 1.0 Transitional //EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
                <html xmlns="http://www.w3.org/1999/xhtml" xmlns:v="urn:schemas-microsoft-com:vml" xmlns:o="urn:schemas-microsoft-com:office:office">
                <head>
                <!--[if gte mso 9]>
                <xml>
                  <o:OfficeDocumentSettings>
                    <o:AllowPNG/>
                    <o:PixelsPerInch>96</o:PixelsPerInch>
                  </o:OfficeDocumentSettings>
                </xml>
                <![endif]-->
                  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <meta name="x-apple-disable-message-reformatting">
                  <!--[if !mso]><!--><meta http-equiv="X-UA-Compatible" content="IE=edge"><!--<![endif]-->
                  <title></title>
                  
                    <style type="text/css">
                      @media only screen and (min-width: 620px) {
                  .u-row {
                    width: 600px !important;
                  }
                  .u-row .u-col {
                    vertical-align: top;
                  }
                
                  .u-row .u-col-100 {
                    width: 600px !important;
                  }
                
                }
                
                @media (max-width: 620px) {
                  .u-row-container {
                    max-width: 100% !important;
                    padding-left: 0px !important;
                    padding-right: 0px !important;
                  }
                  .u-row .u-col {
                    min-width: 320px !important;
                    max-width: 100% !important;
                    display: block !important;
                  }
                  .u-row {
                    width: 100% !important;
                  }
                  .u-col {
                    width: 100% !important;
                  }
                  .u-col > div {
                    margin: 0 auto;
                  }
                }
                body {
                  margin: 0;
                  padding: 0;
                }
                
                table,
                tr,
                td {
                  vertical-align: top;
                  border-collapse: collapse;
                }
                
                p {
                  margin: 0;
                }
                
                .ie-container table,
                .mso-container table {
                  table-layout: fixed;
                }
                
                * {
                  line-height: inherit;
                }
                
                a[x-apple-data-detectors='true'] {
                  color: inherit !important;
                  text-decoration: none !important;
                }
                
                table, td { color: #000000; } #u_body a { color: #0000ee; text-decoration: none; } #u_content_text_3 a { color: #000000; text-decoration: underline; } @media (max-width: 480px) { #u_column_2 .v-col-border { border-top: 5px solid #8d95ff !important;border-left: 5px solid #8d95ff !important;border-right: 5px solid #8d95ff !important;border-bottom: 5px solid #8d95ff !important; } #u_content_image_1 .v-container-padding-padding { padding: 0px !important; } #u_content_image_1 .v-src-width { width: auto !important; } #u_content_image_1 .v-src-max-width { max-width: 100% !important; } #u_content_text_2 .v-container-padding-padding { padding: 20px 10px 10px !important; } #u_content_button_1 .v-container-padding-padding { padding: 10px !important; } #u_content_button_1 .v-size-width { width: 65% !important; } #u_content_text_1 .v-container-padding-padding { padding: 10px 10px 40px !important; } #u_content_text_3 .v-container-padding-padding { padding: 10px 10px 20px !important; } }
                    </style>
                  
                  
                
                <!--[if !mso]><!--><link href="https://fonts.googleapis.com/css?family=Raleway:400,700&display=swap" rel="stylesheet" type="text/css"><!--<![endif]-->
                
                </head>
                
                <body class="clean-body u_body" style="margin: 0;padding: 0;-webkit-text-size-adjust: 100%;background-color: #ecf0f1;color: #000000">
                  <!--[if IE]><div class="ie-container"><![endif]-->
                  <!--[if mso]><div class="mso-container"><![endif]-->
                  <table id="u_body" style="border-collapse: collapse;table-layout: fixed;border-spacing: 0;mso-table-lspace: 0pt;mso-table-rspace: 0pt;vertical-align: top;min-width: 320px;Margin: 0 auto;background-color: #ecf0f1;width:100%" cellpadding="0" cellspacing="0">
                  <tbody>
                  <tr style="vertical-align: top">
                    <td style="word-break: break-word;border-collapse: collapse !important;vertical-align: top">
                    <!--[if (mso)|(IE)]><table width="100%" cellpadding="0" cellspacing="0" border="0"><tr><td align="center" style="background-color: #ecf0f1;"><![endif]-->
                    
                  
                  
                <div class="u-row-container" style="padding: 0px;background-color: transparent">
                  <div class="u-row" style="margin: 0 auto;min-width: 320px;max-width: 600px;overflow-wrap: break-word;word-wrap: break-word;word-break: break-word;background-color: transparent;">
                    <div style="border-collapse: collapse;display: table;width: 100%;height: 100%;background-color: transparent;">
                      <!--[if (mso)|(IE)]><table width="100%" cellpadding="0" cellspacing="0" border="0"><tr><td style="padding: 0px;background-color: transparent;" align="center"><table cellpadding="0" cellspacing="0" border="0" style="width:600px;"><tr style="background-color: transparent;"><![endif]-->
                      
                <!--[if (mso)|(IE)]><td align="center" width="570" class="v-col-border" style="background-color: #ffffff;width: 570px;padding: 0px;border-top: 15px solid #8d95ff;border-left: 15px solid #8d95ff;border-right: 15px solid #8d95ff;border-bottom: 15px solid #8d95ff;border-radius: 0px;-webkit-border-radius: 0px; -moz-border-radius: 0px;" valign="top"><![endif]-->
                <div id="u_column_2" class="u-col u-col-100" style="max-width: 320px;min-width: 600px;display: table-cell;vertical-align: top;">
                  <div style="background-color: #ffffff;height: 100%;width: 100% !important;border-radius: 0px;-webkit-border-radius: 0px; -moz-border-radius: 0px;">
                  <!--[if (!mso)&(!IE)]><!--><div class="v-col-border" style="box-sizing: border-box; height: 100%; padding: 0px;border-top: 15px solid #8d95ff;border-left: 15px solid #8d95ff;border-right: 15px solid #8d95ff;border-bottom: 15px solid #8d95ff;border-radius: 0px;-webkit-border-radius: 0px; -moz-border-radius: 0px;"><!--<![endif]-->
                  
                <table id="u_content_image_1" style="font-family:'Raleway',sans-serif;" role="presentation" cellpadding="0" cellspacing="0" width="100%" border="0">
                  <tbody>
                    <tr>
                      <td class="v-container-padding-padding" style="overflow-wrap:break-word;word-break:break-word;padding:0px;font-family:'Raleway',sans-serif;" align="left">
                        
                <table width="100%" cellpadding="0" cellspacing="0" border="0">
                  <tr>
                    <td style="padding-right: 0px;padding-left: 0px;" align="center">
                      
                      <img align="center" border="0" src="cid:csiheader" alt="image" title="image" style="outline: none;text-decoration: none;-ms-interpolation-mode: bicubic;clear: both;display: inline-block !important;border: none;height: auto;float: none;width: 100%;max-width: 600px;" width="600" class="v-src-width v-src-max-width"/>
                      
                    </td>
                  </tr>
                </table>
                
                      </td>
                    </tr>
                  </tbody>
                </table>
                
                <table id="u_content_text_2" style="font-family:'Raleway',sans-serif;" role="presentation" cellpadding="0" cellspacing="0" width="100%" border="0">
                  <tbody>
                    <tr>
                      <td class="v-container-padding-padding" style="overflow-wrap:break-word;word-break:break-word;padding:20px 40px 10px;font-family:'Raleway',sans-serif;" align="left">
                        
                  <div style="font-size: 14px; line-height: 140%; text-align: left; word-wrap: break-word;">
                    <p style="line-height: 140%;"><span data-metadata="&lt;!--(figmeta)eyJmaWxlS2V5IjoiTHJJMHBSU20xM203UWc0Tk1XZXVQeCIsInBhc3RlSUQiOjE4NjYyMDI5OTgsImRhdGFUeXBlIjoic2NlbmUifQo=(/figmeta)--&gt;" style="line-height: 19.6px;"></span>Hey ${userFirstName}, <br /><br />We've received a request to reset your password for the CSI-Management app. Click the button below to proceed with resetting your password. </p>
                  </div>
                
                      </td>
                    </tr>
                  </tbody>
                </table>
                
                <table id="u_content_button_1" style="font-family:'Raleway',sans-serif;" role="presentation" cellpadding="0" cellspacing="0" width="100%" border="0">
                  <tbody>
                    <tr>
                      <td class="v-container-padding-padding" style="overflow-wrap:break-word;word-break:break-word;padding:10px 10px 10px 40px;font-family:'Raleway',sans-serif;" align="left">
                        
                  <!--[if mso]><style>.v-button {background: transparent !important;}</style><![endif]-->
                <div align="center">
                  <!--[if mso]><v:roundrect xmlns:v="urn:schemas-microsoft-com:vml" xmlns:w="urn:schemas-microsoft-com:office:word" href="https://csiapp.dbit.in/" style="height:36px; v-text-anchor:middle; width:154px;" arcsize="14%"  strokecolor="#000000" strokeweight="1px" fillcolor="#8d95ff"><w:anchorlock/><center style="color:#000000;"><![endif]-->
                    <a href="${magicLink}" target="_blank" class="v-button v-size-width" style="box-sizing: border-box;display: inline-block;text-decoration: none;-webkit-text-size-adjust: none;text-align: center;color: #000000; background-color: #8d95ff; border-radius: 5px;-webkit-border-radius: 5px; -moz-border-radius: 5px; width:30%; max-width:100%; overflow-wrap: break-word; word-break: break-word; word-wrap:break-word; mso-border-alt: none;border-top-width: 1px; border-top-style: solid; border-top-color: #000000; border-left-width: 1px; border-left-style: solid; border-left-color: #000000; border-right-width: 1px; border-right-style: solid; border-right-color: #000000; border-bottom-width: 1px; border-bottom-style: solid; border-bottom-color: #000000;font-size: 13px;">
                        <span style="display:block;padding:10px 20px;line-height:120%;"><strong>Reset password!</strong></span>
                    </a>
                    <!--[if mso]></center></v:roundrect><![endif]-->
                </div>
                
                      </td>
                    </tr>
                  </tbody>
                </table>
                
                <table id="u_content_text_1" style="font-family:'Raleway',sans-serif;" role="presentation" cellpadding="0" cellspacing="0" width="100%" border="0">
                  <tbody>
                    <tr>
                      <td class="v-container-padding-padding" style="overflow-wrap:break-word;word-break:break-word;padding:20px 40px 30px;font-family:'Raleway',sans-serif;" align="left">
                        
                  <div style="font-size: 14px; font-weight: 700; line-height: 140%; text-align: center; word-wrap: break-word;">
                    <p style="line-height: 140%;"><span data-metadata="&lt;!--(figmeta)eyJmaWxlS2V5IjoiTHJJMHBSU20xM203UWc0Tk1XZXVQeCIsInBhc3RlSUQiOjE4NjYyMDI5OTgsImRhdGFUeXBlIjoic2NlbmUifQo=(/figmeta)--&gt;" style="line-height: 19.6px;"></span><em>This email is valid for the next 3 minutes only!</em></p>
                  </div>
                
                      </td>
                    </tr>
                  </tbody>
                </table>
                
                <table style="font-family:'Raleway',sans-serif;" role="presentation" cellpadding="0" cellspacing="0" width="100%" border="0">
                  <tbody>
                    <tr>
                      <td class="v-container-padding-padding" style="overflow-wrap:break-word;word-break:break-word;padding:0px;font-family:'Raleway',sans-serif;" align="left">
                        
                <table width="100%" cellpadding="0" cellspacing="0" border="0">
                  <tr>
                    <td style="padding-right: 0px;padding-left: 0px;" align="center">
                      
                      <img align="center" border="0" src="cid:resetpass" alt="image" title="image" style="outline: none;text-decoration: none;-ms-interpolation-mode: bicubic;clear: both;display: inline-block !important;border: none;height: auto;float: none;width: 90%;max-width: 540px;" width="540" class="v-src-width v-src-max-width"/>
                      
                    </td>
                  </tr>
                </table>
                
                      </td>
                    </tr>
                  </tbody>
                </table>
                
                  <!--[if (!mso)&(!IE)]><!--></div><!--<![endif]-->
                  </div>
                </div>
                <!--[if (mso)|(IE)]></td><![endif]-->
                      <!--[if (mso)|(IE)]></tr></table></td></tr></table><![endif]-->
                    </div>
                  </div>
                  </div>
                  
                
                
                  
                  
                <div class="u-row-container" style="padding: 0px;background-color: transparent">
                  <div class="u-row" style="margin: 0 auto;min-width: 320px;max-width: 600px;overflow-wrap: break-word;word-wrap: break-word;word-break: break-word;background-color: transparent;">
                    <div style="border-collapse: collapse;display: table;width: 100%;height: 100%;background-color: transparent;">
                      <!--[if (mso)|(IE)]><table width="100%" cellpadding="0" cellspacing="0" border="0"><tr><td style="padding: 0px;background-color: transparent;" align="center"><table cellpadding="0" cellspacing="0" border="0" style="width:600px;"><tr style="background-color: transparent;"><![endif]-->
                      
                <!--[if (mso)|(IE)]><td align="center" width="600" class="v-col-border" style="background-color: #8d95ff;width: 600px;padding: 0px;border-top: 0px solid transparent;border-left: 0px solid transparent;border-right: 0px solid transparent;border-bottom: 0px solid transparent;border-radius: 0px;-webkit-border-radius: 0px; -moz-border-radius: 0px;" valign="top"><![endif]-->
                <div class="u-col u-col-100" style="max-width: 320px;min-width: 600px;display: table-cell;vertical-align: top;">
                  <div style="background-color: #8d95ff;height: 100%;width: 100% !important;border-radius: 0px;-webkit-border-radius: 0px; -moz-border-radius: 0px;">
                  <!--[if (!mso)&(!IE)]><!--><div class="v-col-border" style="box-sizing: border-box; height: 100%; padding: 0px;border-top: 0px solid transparent;border-left: 0px solid transparent;border-right: 0px solid transparent;border-bottom: 0px solid transparent;border-radius: 0px;-webkit-border-radius: 0px; -moz-border-radius: 0px;"><!--<![endif]-->
                  
                <table id="u_content_text_3" style="font-family:'Raleway',sans-serif;" role="presentation" cellpadding="0" cellspacing="0" width="100%" border="0">
                  <tbody>
                    <tr>
                      <td class="v-container-padding-padding" style="overflow-wrap:break-word;word-break:break-word;padding:0px 0px 10px;font-family:'Raleway',sans-serif;" align="left">
                        
                  <div style="font-size: 14px; color: #000000; line-height: 170%; text-align: center; word-wrap: break-word;">
                    <p style="line-height: 170%;"><a rel="noopener" href="https://csiapp.dbit.in/" target="_blank">Contact us</a>   |   <a rel="noopener" href="https://csiapp.dbit.in/" target="_blank">Privacy Policy</a>   |   <a rel="noopener" href="https://csiapp.dbit.in/" target="_blank">CSI Web</a></p>
                  </div>
                
                      </td>
                    </tr>
                  </tbody>
                </table>
                
                <table style="font-family:'Raleway',sans-serif;" role="presentation" cellpadding="0" cellspacing="0" width="100%" border="0">
                  <tbody>
                    <tr>
                      <td class="v-container-padding-padding" style="overflow-wrap:break-word;word-break:break-word;padding:0px;font-family:'Raleway',sans-serif;" align="left">
                        
                  <table height="0px" align="center" border="0" cellpadding="0" cellspacing="0" width="100%" style="border-collapse: collapse;table-layout: fixed;border-spacing: 0;mso-table-lspace: 0pt;mso-table-rspace: 0pt;vertical-align: top;border-top: 1px solid #BBBBBB;-ms-text-size-adjust: 100%;-webkit-text-size-adjust: 100%">
                    <tbody>
                      <tr style="vertical-align: top">
                        <td style="word-break: break-word;border-collapse: collapse !important;vertical-align: top;font-size: 0px;line-height: 0px;mso-line-height-rule: exactly;-ms-text-size-adjust: 100%;-webkit-text-size-adjust: 100%">
                          <span>&#160;</span>
                        </td>
                      </tr>
                    </tbody>
                  </table>
                
                      </td>
                    </tr>
                  </tbody>
                </table>
                
                  <!--[if (!mso)&(!IE)]><!--></div><!--<![endif]-->
                  </div>
                </div>
                <!--[if (mso)|(IE)]></td><![endif]-->
                      <!--[if (mso)|(IE)]></tr></table></td></tr></table><![endif]-->
                    </div>
                  </div>
                  </div>
                  
                
                
                    <!--[if (mso)|(IE)]></td></tr></table><![endif]-->
                    </td>
                  </tr>
                  </tbody>
                  </table>
                  <!--[if mso]></div><![endif]-->
                  <!--[if IE]></div><![endif]-->
                </body>
                
                </html>
                
                `,
                attachments: [
                    {   // Attached image 1
                        filename: 'csiheader.png',
                        content: imageData1,
                        encoding: 'base64',
                        cid: 'csiheader' // Set the CID here
                    },
                    {   // Attached image 2
                        filename: 'resetpass.png',
                        content: imageData2,
                        encoding: 'base64',
                        cid: 'resetpass' // Set the CID here
                    }
                ]
            };
    
            // Send email
            transporter.sendMail(mailOptions, function(error, info){
                if (error) {
                    console.log("Error sending magic link email:", error);
                } else {
                    console.log("Magic link email sent:", info.response);
                }
            });
        });
    });
}

// Route to handle the reset password link
router.get('/resetpassword/:token', (req, res) => {
    const token = req.params.token;

    // Check if the token exists and is valid
    // If valid, check if it has expired
    connection.query('SELECT pass_reset_token_expiration FROM core_details WHERE pass_reset_token = ?', [token], (error, result) => {
        if (error) {
            console.log("Error fetching token expiration from the database:", error);
            res.sendStatus(500);
            return;
        }

        if (result.length === 0) {
            // Token not found
            console.log("Token not found");
            res.sendStatus(404);
            return;
        }

        const expirationTime = result[0].pass_reset_token_expiration;

        if (expirationTime < new Date().getTime()) {
            // Token has expired
            console.log("Token has expired");
            res.sendStatus(410); // Gone (expired)
            return;
        }

        // Token is valid and not expired
        // Render the sign-up page
        res.render('pages/sign-up', { Emsg: req.flash("Emsg") });
    });
});


// Route to handle password reset
router.post('/newpassword', (req, res) => {
    const token = req.body.token; // Get token from the request body
    const newPassword = req.body.newPassword;
  
    console.log(token , newPassword);
    // Hash the password using MD5 algorithm
    const hashedPassword = crypto.createHash('md5').update(newPassword).digest('hex');
  
    // Update the hashed password in the database based on the token
    connection.query('UPDATE core_details SET core_pwd = ? WHERE pass_reset_token = ?', [hashedPassword, token], (error, result) => {
      if (error) {
        console.error("Error updating password:", error);
        res.status(500).json({ message: "Internal Server Error" });
      } else {
        if (result.affectedRows > 0) {
          res.status(200).json({ message: "Password updated" });
        } else {
          res.status(404).json({ message: "Invalid or expired token" });
        }
      }
    });
});



module.exports = router;