var express = require('express');
var router = express.Router();
const multer = require('multer');
const path = require('path');
const app = express();
const fs = require('fs');
const request = require('request');
var bodyParser=require('body-parser');

// Increase payload limit to 50MB
app.use(bodyParser.json({ limit: '50mb' }));
app.use(bodyParser.urlencoded({ limit: '50mb', extended: true }));

// MySQL Connection
var mysql = require('mysql');
var connection = mysql.createConnection({
    host: '128.199.23.207',
    user: "csi",
    password: "csi",
    database: 'csiApp2022'
});
connection.connect(function (err) {
    if (err) {
        console.log('Not Connected to core Publicity');
    }
    else {
        console.log("Connected To core Publicity");
    }
});

// Create a storage object with Multer to handle file uploads

const storage = multer.diskStorage({
    destination: function (req, file, cb) {
      cb(null, "server_uploads/publicity_pdf/"); // Set the destination folder for uploaded files
    },
    filename: function (req, file, cb) {
      cb(null, file.originalname); // Set the filename to the original filename of the uploaded file
    },
  });
  
  const upload = multer({ 
    limits: { fileSize: 10000000 },
    storage: storage });
  
  // Create a route to handle file uploads
  router.post("/upload", upload.single("pdfFile"), function (req, res, next) {
    const file = req.file;
    const eid = req.body.eid;
    console.log(eid);
  
    // Check if a file was uploaded
    if (!file) {
      const error = new Error("Please upload a file");
      error.httpStatusCode = 400;
      return next(error);
    }
  
    // Insert the file URL into the MySQL database
    const url = "http://localhost:3000/server_uploads/publicity_pdf/" + file.filename; // Set the file URL to the server URL and the filename
    const size = file.size;
    const query = "INSERT INTO files (content, name, pdf_cpm_id, size) VALUES (?, ?, ?, ?)"; // Replace "files" with the name of your MySQL table
  
    connection.query(query, [ url, file.filename, eid, size ], function (error, results, fields) {
      if (error) throw error;
      res.send("File uploaded successfully");
    });
  });
  
// Create a route to handle file downloads
router.get("/download", function (req, res) {
    const eid = req.query.eid;
  
    // Query the MySQL database to get the file URL and filename based on the "eid" parameter
    const query = "SELECT content, name FROM files WHERE pdf_cpm_id = ?";
    connection.query(query, [eid], function (error, results, fields) {
      if (error) {
        console.error(error);
        return res.status(500).send("Error retrieving file from database");
      }
  
      if (!results || !results.length) {
        return res.status(404).send("File not found");
      }
  
      const url = results[0].content;
      const filename = results[0].name;

// Extract the file path from the URL
const filePath = url.split('/').pop();

// Create an absolute path based on your server's file system
const absolutePath = path.join(__dirname, '..', 'server_uploads', 'publicity_pdf', filePath);

// Set the headers for the download response
res.setHeader("Content-disposition", "attachment; filename=" + filename);
res.setHeader("Content-type", "application/pdf");

// Send the file to the client
res.sendFile(absolutePath, function (error) {
  if (error) {
    console.error(error);
    res.status(error.status).end();
  } else {
    console.log("Sent file:", absolutePath);
  }
});

    });
  });
  

//Viewing Events
router.post('/viewEvent', (req, res) => {
    var eid = req.body.eid;

    const sql = "SELECT e.proposals_event_name, e.proposals_event_category, e.proposals_event_date, e.speaker, e.proposals_venue, e.proposals_reg_fee_csi, e.proposals_reg_fee_noncsi, e.proposals_prize, e.proposals_desc, e.proposals_creative_budget, e.proposals_publicity_budget, e.proposals_guest_budget, p.pr_desk_publicity, p.pr_class_publicity, p.pr_member_count, p.pr_comment, p.pr_rcd_amount, p.pr_spent, (SELECT GROUP_CONCAT(pt.pub_tasks) FROM publicity_tasks pt WHERE pt.cpm_id = p.cpm_id) AS tasks,(SELECT GROUP_CONCAT(pt.status) FROM publicity_tasks pt WHERE pt.cpm_id = p.cpm_id) AS status FROM core_proposals_manager e INNER JOIN core_pr_manager p ON e.cpm_id = p.cpm_id WHERE p.cpm_id = ?;"
    connection.query(sql, [eid], function (error, results) {
        if (error) {
            console.log("Failed To view Publicity events");
            res.sendStatus(400);
        }
        else {
            console.log("Successfully viewed Publicity events");
            console.log(results);
            try {
                const tasksArray = results[0].tasks.split(','); // split the comma-separated string into an array
                results[0].tasks = tasksArray; // replace the tasks property with the array
                console.log(tasksArray);
            } catch (error) {
                TypeError
            }
            res.status(200).send(results[0]);
        }
    });
});
//Adding checkboxes

router.post('/addcheckbox', (req, res) => {
    var eid = req.body.eid;
    console.log(eid);
    var checkedCheckboxes = req.body.checkedCheckboxes;
    lenghtofarray = Object.keys(checkedCheckboxes || {}).length
    console.log(lenghtofarray);
    var checkboxStatus = req.body.checkboxStatus;
    // Store the checkedCheckboxes list in the MySQL database
    for (let i = 0; i < lenghtofarray; i++) {

        const checked = 1;
        const checkbox = checkedCheckboxes[i];
        const boxstatus = checkboxStatus[i];
        console.log(boxstatus);

        // Check if the checkbox is already present in the table
        connection.query("SELECT * FROM publicity_tasks WHERE pub_tasks=? AND cpm_id=?", [checkbox, eid], (error, results) => {
            if (error) {
                console.error("Error aa raha hai", error);
                res.sendStatus(500);
                return;
            } else {
                if (results.length > 0) {
                    console.log("Checkbox already present");
                    return;
                } else {
                    connection.query("INSERT INTO publicity_tasks (pub_tasks, status , cpm_id) VALUES (?, ? , ?)", [checkbox, boxstatus, eid], (error, results) => {
                        if (error) {
                            console.error("Error aa raha hai", error);
                            res.sendStatus(500);
                            return;
                        } else {
                            console.log("Ho gai entryy", results);
                        }
                    });
                }
            }
        });
    }
});

//Edit form
router.post('/editPublicity', (req, res) => {
    var pr_rcd_amount = req.body.pr_rcd_amount;
    var pr_desk_publicity = req.body.pr_desk_publicity;
    var pr_class_publicity = req.body.pr_class_publicity;
    var pr_member_count = req.body.pr_member_count;
    var pr_comment = req.body.pr_comment;
    var eid = req.body.eid;
    var pr_spent = req.body.pr_spent;

    //pushing into publicty table 
    connection.query('UPDATE core_pr_manager SET pr_desk_publicity=?, pr_class_publicity=?,pr_member_count=?,pr_comment=?, pr_rcd_amount=?,pr_spent =? ,pr_status=3 WHERE cpm_id=?', [pr_desk_publicity, pr_class_publicity,pr_member_count, pr_comment, pr_rcd_amount, pr_spent, eid], function (error) {
        if (error) {
            console.log(error);
            console.log("Failed to edit publicity event");
            res.sendStatus(400);
        }
        else {
            console.log("Sucessfully updated publicty event");
            res.sendStatus(200);
        }
    });
});

//Fetching added checkboxes

router.get('/checkboxes', function (req, res) {
    const status = 1;
    const sql = 'SELECT task FROM `publicity_tasks` WHERE status = 1';
    connection.query(sql, [status], function (error, results, fields) {
        if (error) {
            console.error(error);
            res.status(500).send('Error retrieving checkboxes');
        } else {
            console.log(results);
            const checkboxes = results.map(result => result.name);
            res.json(checkboxes);
        }
    });
});

module.exports = router;