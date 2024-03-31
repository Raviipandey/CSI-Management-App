var express = require('express');
var router = express.Router();
var randomstring = require('randomstring');
var dotenv = require('dotenv');
dotenv.config();
const validateSessionToken  = require('../middleware/ValidateTokens');
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
        console.log('Connected to new proposals');
    } else {
        console.log("Not Connected to new proposals");
    }
});

//Email Connection
var nodemailer = require('nodemailer');
var transporter = nodemailer.createTransport({
    service: 'gmail',
    auth: {
        user: process.env.username,
        pass: process.env.pass
    }
});


// router.post('/addproposal' , (req , res) => {
//     var id = req.query.id;
//     var name = req.query.name;
//     var date = req.query.date;
//     var category = req.query.category;
//     var venue = req.query.venue;
//     var threetrack = req.query.threetrack;
//     var desc = req.query.desc;
//     var budget = req.query.budget;
//     var reg_fee_c = req.query.reg_fee_c;
//     var reg_fee_nc = req.query.reg_fee_nc;
//     var prize = req.query.prize;
//     var publicity = req.body.pb;
//     var creative = req.body.cb;
//     var guest = req.body.gb;
//     var others = JSON.stringify(req.body.ob);
    
//     connection.query("INSERT INTO core_proposals_manager(cpm_id,proposals_event_name,proposals_event_date,proposals_event_category,proposals_venue,proposals_three_track, proposals_desc,proposals_total_budget,proposals_reg_fee_csi,proposals_reg_fee_noncsi,proposals_prize) VALUES (?,?,?,?,?,?,?,?,?,?,?)" , [id , name , date , category , venue , threetrack , desc , budget , reg_fee_c , reg_fee_nc , prize] , function(error){
//         if (error) {
//             console.log(error)
//             console.log("Failed to add proposal");
//             res.sendStatus(400);
//         } else {
//             console.log("Succesfully added");
//             res.sendStatus(200);
//         }
//     })
// })


//Creating propsal
router.post('/createproposal',validateSessionToken, (req, res) => {
    var cpm_id = req.body.cpm_id;
    var proposals_event_name = req.body.proposals_event_name;
    var proposals_event_category = req.body.proposals_event_category;
    var proposals_three_track = req.body.proposals_three_track;
    var proposals_event_date = req.body.proposals_event_date;
    var speaker = req.body.speaker;
    var proposals_venue = req.body.proposals_venue;
    var proposals_reg_fee_csi = req.body.proposals_reg_fee_csi;
    var proposals_reg_fee_noncsi = req.body.proposals_reg_fee_noncsi;
    var proposals_prize = req.body.proposals_prize;
    var proposals_desc = req.body.proposals_desc;
    var proposals_total_budget = req.body.proposals_total_budget;
    var proposals_creative_budget = req.body.cb;
    var proposals_publicity_budget = req.body.pb;
    var proposals_guests_budget = req.body.gb;
    var proposals_others_budget = JSON.stringify(req.body.ob);
    var agenda = req.body.agenda;
    // var date = req.body.date;
    // var creative_budget = req.body.cb;
    // var publicity_budget = req.body.pb;
    // var guest_budget = req.body.gb;
    // var event_date = req.body.e_date;
    // var others_budget = JSON.stringify(req.body.ob);
    connection.query('INSERT INTO core_proposals_manager(proposals_event_name , proposals_event_category , proposals_three_track , proposals_event_date, speaker, proposals_venue , proposals_reg_fee_csi ,proposals_reg_fee_noncsi ,proposals_prize , proposals_desc , proposals_creative_budget, proposals_publicity_budget, proposals_guest_budget, proposals_others_budget, proposals_total_budget ,agenda) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)', [proposals_event_name , proposals_event_category , proposals_three_track , proposals_event_date, speaker, proposals_venue , proposals_reg_fee_csi ,proposals_reg_fee_noncsi ,proposals_prize , proposals_desc,  proposals_creative_budget , proposals_publicity_budget, proposals_guests_budget,  proposals_others_budget ,proposals_total_budget ,agenda ], function(error) {
        if (error) {
            console.log(error)
            console.log("Fail to insert into events table");
            res.sendStatus(400);
        } else {
            console.log("Succesfully inserted into events table");
            res.sendStatus(200);
        }
    });
});


router.get('/getalltoken',validateSessionToken, (req, res) => {
    connection.query('SELECT core_id, fcm_token FROM csiApp2022.core_details WHERE core_role_id NOT IN (1, 2)', function(error, result) {
        if (error) {
            console.log("Error fetching core fcm tokens");
            res.status(400).json({ error: 'Error fetching core fcm tokens' });
        } else {
            // Extract core_id and fcm_token values from the result array and create an array of objects
            const tokensWithIds = result.map(row => {
                return {
                    core_id: row.core_id,
                    fcm_token: row.fcm_token
                };
            }).filter(obj => obj.fcm_token); // Filter out null or empty tokens

            // Send the tokensWithIds array as JSON
            res.status(200).json(tokensWithIds);
        }
    });
});


router.get('/getadmintoken',validateSessionToken,(req, res) => {
    var id = req.query.id; // Access id from query parameters
    console.log("Received id: ", id);
    connection.query('SELECT core_id, fcm_token FROM csiApp2022.core_details WHERE core_role_id = ?', [id], function(error, result) {
        if (error) {
            console.log("Error fetching core fcm tokens:", error);
            res.status(400).json({ error: 'Error fetching core fcm tokens' });
        } else {
            // Extract core_id and fcm_token values from the result array and create an array of objects
            const tokensWithIds = result.map(row => {
                return {
                    core_id: row.core_id,
                    fcm_token: row.fcm_token
                };
            }).filter(obj => obj.fcm_token); // Filter out null or empty tokens

            // Send the tokensWithIds array as JSON
            res.status(200).json(tokensWithIds);
        }
    });
});

//This route is used for attendance
router.get('/getcvctoken',validateSessionToken,(req, res) => {
    connection.query('SELECT fcm_token FROM csiApp2022.core_details WHERE core_role_id IN (3 , 4)', function(error, result) {

router.get('/getalltoken', (req, res) => {
    connection.query('SELECT core_id, fcm_token FROM csiApp2022.core_details WHERE core_role_id NOT IN (1, 2)', function(error, result) {

        if (error) {
            console.log("Error fetching core fcm tokens");
            res.status(400).json({ error: 'Error fetching core fcm tokens' });
        } else {
            // Extract core_id and fcm_token values from the result array and create an array of objects
            const tokensWithIds = result.map(row => {
                return {
                    core_id: row.core_id,
                    fcm_token: row.fcm_token
                };
            }).filter(obj => obj.fcm_token); // Filter out null or empty tokens

            // Send the tokensWithIds array as JSON
            res.status(200).json(tokensWithIds);
        }
    });
});


//Tech head and event head token
router.get('/getthehtoken',validateSessionToken, (req, res) => {
    connection.query('SELECT core_id, fcm_token FROM csiApp2022.core_details WHERE core_role_id IN (5 ,6)', function(error, result) {


router.get('/getadmintoken', (req, res) => {
    var id = req.query.id; // Access id from query parameters
    console.log("Received id: ", id);
    connection.query('SELECT core_id, fcm_token FROM csiApp2022.core_details WHERE core_role_id = ?', [id], function(error, result) {
        if (error) {
            console.log("Error fetching core fcm tokens:", error);
            res.status(400).json({ error: 'Error fetching core fcm tokens' });
        } else {
            // Extract core_id and fcm_token values from the result array and create an array of objects
            const tokensWithIds = result.map(row => {
                return {
                    core_id: row.core_id,
                    fcm_token: row.fcm_token
                };
            }).filter(obj => obj.fcm_token); // Filter out null or empty tokens

            // Send the tokensWithIds array as JSON
            res.status(200).json(tokensWithIds);
        }
    });
});


// This route is used for attendance
router.get('/getcvctoken', (req, res) => {
    connection.query('SELECT core_id, fcm_token FROM csiApp2022.core_details WHERE core_role_id IN (3 , 4)', function(error, result) {

        if (error) {
            console.log("Error fetching core fcm tokens");
            res.status(400).json({ error: 'Error fetching core fcm tokens' });
        } else {
            const tokensWithIds = result.map(row => {
                return {
                    core_id: row.core_id,
                    fcm_token: row.fcm_token
                };
            }).filter(obj => obj.fcm_token);



            // Send the tokensWithIds array as JSON
            res.status(200).json(tokensWithIds);
        }
    });
});

//Tech head and event head token
router.get('/getthehtoken', (req, res) => {
    connection.query('SELECT core_id, fcm_token FROM csiApp2022.core_details WHERE core_role_id IN (5 ,6)', function(error, result) {
        if (error) {
            console.log("Error fetching core fcm tokens");
            res.status(400).json({ error: 'Error fetching core fcm tokens' });
        } else {
            const tokensWithIds = result.map(row => {
                return {
                    core_id: row.core_id,
                    fcm_token: row.fcm_token
                };
            }).filter(obj => obj.fcm_token);


            // Send the fcmTokens array as JSON
            res.status(200).json(tokensWithIds);
        }
    });
});

// cpm_id, proposals_event_name, proposals_event_date, proposals_event_category, proposals_venue, 
// proposals_three_track, proposals_desc, proposals_total_budget, proposals_reg_fee_csi, proposals_reg_fee_noncsi, 
// proposals_prize, proposals_meeting_id, proposals_status, proposals_comment, agenda, speaker
//search for agenda
router.post('/viewagenda',validateSessionToken, (req, res) => {
    var date = req.body.date;
    console.log(date);
    connection.query('SELECT minute_objective FROM core_minute_manager where minute_date = ?', [date], function(error, results) {
        if (error) {
            console.log("Fail to view agenda");
            res.sendStatus(400);
        } else {
            for (var i = 0; i < results.length; i++) {
                results[i] = results[i].minute_objective;
            }
            console.log("Successfully viewed agenda");
            console.log(results);
            res.status(200).send({ "agenda": results });
        }
    });
});

//modify status
// router.post('/status', (req, res) => {
//     var cpm_id = req.body.cpm_id;
//     var status = req.body.proposals_status;
//     var comment = req.body.proposals_comment;

//     connection.query('UPDATE core_proposals_manager SET proposals_status=?, proposals_comment=? WHERE cpm_id=?', [status, comment, cpm_id], function(error) {
//         if (error) {
//             console.log(error);
//             console.log("Fail to update status");
//             res.sendStatus(400);
//         } else {
//             //Mail to Creative-head/PR-head/Tech-head
//             if (status == 2) {
//                 connection.query("INSERT INTO core_creative_manager(cpm_id) VALUES(?)", [cpm_id], function(error) {
//                     if (error) {
//                         console.log("Fail To Insert Into Creative Table");
//                         res.sendStatus(400);
//                     } else {
//                         console.log("Succesfully Inserted Into Creative Table");
//                         connection.query("INSERT INTO core_pr_manager(cpm_id) VALUES(?)", [cpm_id], function(error) {
//                             if (error) {
//                                 console.log(error);
//                                 console.log("Fail To Insert Into Publicity Table");
//                                 res.sendStatus(400);
//                             } else {
//                                 console.log("Succesfully Inserted Into Publicity Table");
//                                 connection.query("INSERT INTO core_technical_manager(cpm_id) VALUES(?)", [cpm_id], function(error) {
//                                     if (error) {
//                                         console.log("Fail To Insert Into Technical Table");
//                                         res.sendStatus(400);
//                                     } else {
//                                         console.log("Succesfully Inserted Into Technical Table");
//                                         res.sendStatus(200);
//                                     }

//                                 });
//                             }
//                         });
//                     }
//                 });

//             } else {
//                 console.log("Succesfully updated  status");
//                 res.sendStatus(200);
//             }
//         }
//     });
// });

router.post('/status',validateSessionToken,(req, res) => {
    const cpm_id = req.body.cpm_id;
    const status = req.body.proposals_status;
    const newRole = req.body.role; // Retrieve the role from the request
    const newComment = req.body.proposals_comment;

    // First, fetch the existing comments
    connection.query('SELECT proposals_comment FROM core_proposals_manager WHERE cpm_id=?', [cpm_id], function(error, results) {
        if (error) {
            console.log(error);
            return res.sendStatus(400);
        } else {
            let comments = {};
            if (results.length > 0 && results[0].proposals_comment) {
                // Parse existing comments if there are any
                comments = JSON.parse(results[0].proposals_comment);
            }
            // Add or update the comment for the current role
            comments[newRole] = newComment;

            // Now update the database with the new comments JSON
            connection.query('UPDATE core_proposals_manager SET proposals_status=?, proposals_comment=? WHERE cpm_id=?', 
                [status, JSON.stringify(comments), cpm_id], function(updateError) {
                    if (updateError) {
                        console.log(updateError);
                        return res.sendStatus(400);
                    } else {
                        if (status === '3') {
                            connection.query('INSERT INTO core_creative_manager (cpm_id) VALUES (?)', [cpm_id], function(err, res) {
                                if (err) {
                                    console.log(err);
                                    return res.sendStatus(400);
                                }
                                // Insert into core_pr_manager
                                connection.query('INSERT INTO core_pr_manager (cpm_id) VALUES (?)', [cpm_id], function(err, result1) {
                                    if (err) {
                                        console.log(err);
                                        return res.sendStatus(400);
                                    }
                                    // Insert into core_technical_manager
                                    connection.query('INSERT INTO core_technical_manager (cpm_id) VALUES (?)', [cpm_id], function(err, result2) {
                                        if (err) {
                                            console.log(err);
                                            return res.sendStatus(400);
                                        }
                                        console.log("All inserts successful");
                                            return res.sendStatus(200);
                                    });
                                });
                            });
                        } else {
                            // Do something else if status is not equal to '3'
                        }
                        
                        
                        console.log("Successfully updated status and comments");
                        res.sendStatus(200);
                    }
                });
        }
    });
});



//View proposal details
router.post('/viewproposal',validateSessionToken, (req, res) => {
    var cpm_id = req.body.cpm_id;
    connection.query('SELECT proposals_event_name, proposals_event_category,proposals_three_track, proposals_desc, proposals_event_date,proposals_creative_budget,proposals_publicity_budget,proposals_guest_budget,proposals_total_budget,proposals_comment from core_proposals_manager where cpm_id=?;', [cpm_id], function(error, results) {
        console.log(results)
        if (error) {
            console.log("Fail to view proposal");
            res.sendStatus(400);
        } else {
            console.log("Successfully viewed proposal");
            res.status(200).send(results[0]);
        }
    });
});

//Listing All proposal
router.get('/viewlistproposal',validateSessionToken, (req, res) => {

    connection.query('SELECT cpm_id, proposals_event_name, proposals_event_category ,proposals_status , proposals_event_date from core_proposals_manager order by cpm_id DESC ;', function(error, results) {
        // console.log(results)
        if (error) {
            console.log("Fail to list proposal");
            res.sendStatus(400);
        } else {
            console.log("Succedfully listed proposal");
            res.status(200).send(results);
        }
    });
});

//Edit proposal
router.post('/editproposal',validateSessionToken, (req, res) => {
    
    var eid = req.body.eid;
    var name = req.body.name;
    var theme = req.body.theme;
    var description = req.body.description;
    var date = req.body.date;
    var creative_budget = req.body.cb;
    var publicity_budget = req.body.pb;
    var guest_budget = req.body.gb;

    connection.query('UPDATE core_proposals_manager SET proposals_event_name=?, proposals_event_category=?, proposals_desc=?,proposals_event_date=?,proposals_creative_budget=?,proposals_publicity_budget=?,proposals_guest_budget=?,proposals_status=0 WHERE cpm_id=?;', [name, theme, description, date, creative_budget, publicity_budget, guest_budget, eid], function(error, result) {
        if (error) {
            console.log(error);
            console.log("Fail to edit proposal");
            res.sendStatus(400);
        } else {
            console.log("Succesfully edited proposal");
            console.log(result);
            res.sendStatus(200);
        }
    });
});

module.exports = router;