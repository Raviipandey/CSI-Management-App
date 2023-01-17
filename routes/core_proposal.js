var express = require('express');
var router = express.Router();
var randomstring = require('randomstring');
var dotenv = require('dotenv');
dotenv.config();

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


router.post('/addproposal' , (req , res) => {
    var id = req.query.id;
    var name = req.query.name;
    var date = req.query.date;
    var category = req.query.category;
    var venue = req.query.venue;
    var threetrack = req.query.threetrack;
    var desc = req.query.desc;
    var budget = req.query.budget;
    var reg_fee_c = req.query.reg_fee_c;
    var reg_fee_nc = req.query.reg_fee_nc;
    var prize = req.query.prize;
    
    connection.query("INSERT INTO core_proposals_manager(cpm_id,proposals_event_name,proposals_event_date,proposals_event_category,proposals_venue,proposals_three_track, proposals_desc,proposals_total_budget,proposals_reg_fee_csi,proposals_reg_fee_noncsi,proposals_prize) VALUES (?,?,?,?,?,?,?,?,?,?,?)" , [id , name , date , category , venue , threetrack , desc , budget , reg_fee_c , reg_fee_nc , prize] , function(error){
        if (error) {
            console.log(error)
            console.log("Failed to add proposal");
            res.sendStatus(400);
        } else {
            console.log("Succesfully added");
            res.sendStatus(200);
        }
    })
})


//Creating propsal
router.post('/createproposal', (req, res) => {
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
    var agenda = req.body.agenda;
    // var date = req.body.date;
    // var creative_budget = req.body.cb;
    // var publicity_budget = req.body.pb;
    // var guest_budget = req.body.gb;
    // var event_date = req.body.e_date;
    // var others_budget = JSON.stringify(req.body.ob);
    connection.query('INSERT INTO core_proposals_manager(cpm_id , proposals_event_name , proposals_event_category , proposals_three_track , proposals_event_date, speaker, proposals_venue , proposals_reg_fee_csi ,proposals_reg_fee_noncsi ,proposals_prize , proposals_desc , proposals_total_budget ,agenda  ) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)', [Math.random(4) ,proposals_event_name , proposals_event_category , proposals_three_track , proposals_event_date, speaker, proposals_venue , proposals_reg_fee_csi ,proposals_reg_fee_noncsi ,proposals_prize , proposals_desc , proposals_total_budget ,agenda ], function(error) {
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


// cpm_id, proposals_event_name, proposals_event_date, proposals_event_category, proposals_venue, 
// proposals_three_track, proposals_desc, proposals_total_budget, proposals_reg_fee_csi, proposals_reg_fee_noncsi, 
// proposals_prize, proposals_meeting_id, proposals_status, proposals_comment, agenda, speaker
//search for agenda
router.post('/viewagenda', (req, res) => {
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
router.post('/status', (req, res) => {
    var cpm_id = req.body.cpm_id;
    var status = req.body.proposals_status;
    var comment = req.body.proposals_comment;

    connection.query('UPDATE core_proposals_manager SET proposals_status=?, proposals_comment=? WHERE cpm_id=?', [status, comment, cpm_id], function(error) {
        if (error) {
            console.log(error);
            console.log("Fail to update status");
            res.sendStatus(400);
        } else {
            //Mail to Creative-head/PR-head/Tech-head
            if (status == 2) {
                connection.query("SELECT email,name FROM profile WHERE (role='PR Head' or role='Technical Head' or role='Creative Head')", function(err, result) {
                    if (err)
                        console.log("Proposal Email Extraction Error");
                    else {
                        for (var i = 0; i < 3; i++) {
                            var mailOptions = {
                                from: 'csi.managementapp@gmail.com',
                                to: result[i].email,
                                subject: 'CSI-App Event',
                                html: '<p><span style="font-size: 17px;">Greetings <strong>' + result[i].name + '</strong>,</span></p><p>A new event has been created. Please fill your required details!</p><br><br><br>Regards,<br><strong>CSI-Management APP development team.</strong>'
                                    //text: "Hello There!!!!! An event has been created pls fill your respective details"
                            }
                            transporter.sendMail(mailOptions, function(error, info) {
                                if (error) {
                                    console.log("Email Error");
                                    // res.sendStatus(400);
                                } else
                                    console.log('Email sent:' + info.response);
                            });
                        }

                        //Creating events into creative/publicity/technical table
                        connection.query("INSERT INTO creative(eid) VALUES(?)", [eid], function(error) {
                            if (error) {
                                console.log("Fail To Insert Into Creative Table");
                                res.sendStatus(400);
                            } else {
                                console.log("Succesfully Inserted Into Creative Table");
                                connection.query("INSERT INTO publicity(eid) VALUES(?)", [eid], function(error) {
                                    if (error) {
                                        console.log("Fail To Insert Into Publicity Table");
                                        res.sendStatus(400);
                                    } else {
                                        console.log("Succesfully Inserted Into Publicity Table");
                                        connection.query("INSERT INTO technical(eid) VALUES(?)", [eid], function(error) {
                                            if (error) {
                                                console.log("Fail To Insert Into Technical Table");
                                                res.sendStatus(400);
                                            } else {
                                                console.log("Succesfully Inserted Into Technical Table");
                                                res.sendStatus(200);
                                            }

                                        });
                                    }
                                });
                            }
                        });
                    }
                });

            } else {
                console.log("Succesfully updated  status");
                res.sendStatus(200);
            }
        }
    });
});

//View proposal details
router.post('/viewproposal', (req, res) => {
    var cpm_id = req.body.cpm_id;
    connection.query('SELECT * from core_proposals_manager where cpm_id=?;', [cpm_id], function(error, results) {
        // console.log(results)
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
router.get('/viewlistproposal', (req, res) => {

    connection.query('SELECT cpm_id, proposals_event_name, proposals_event_category ,proposals_status , proposals_event_date from core_proposals_manager order by proposals_event_date DESC ;', function(error, results) {
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
router.post('/editproposal', (req, res) => {
    var eid = req.body.eid;
    var name = req.body.name;
    var theme = req.body.theme;
    var description = req.body.description;
    var date = req.body.date;
    var creative_budget = req.body.cb;
    var publicity_budget = req.body.pb;
    var guest_budget = req.body.gb;

    connection.query('UPDATE events SET name=?,theme=?,description=?,event_date=?,creative_budget=?,publicity_budget=?,guest_budget=?,status=0 WHERE eid=?', [name, theme, description, date, creative_budget, publicity_budget, guest_budget, eid], function(error) {
        if (error) {
            console.log("Fail to edit proposal");
            res.sendStatus(400);
        } else {
            console.log("Succesfully edited proposal");
            res.sendStatus(200);
        }
    });
});


module.exports = router;