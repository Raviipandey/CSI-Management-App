var express = require('express');
var router = express.Router();
const multer = require('multer');
const path = require('path');
const app = express();
const fs = require('fs');
const request = require('request');
var bodyParser=require('body-parser');
const { server_url} = require('../serverconfig');
const validateSessionToken  = require('../middleware/ValidateTokens');

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
        cb(null, "server_uploads/publicity_pdf/");
    },
    filename: function (req, file, cb) {
        // Use the original file name or a timestamp-based temporary name
        const tempFilename = Date.now() + path.extname(file.originalname); // Temporary name with original extension
        cb(null, tempFilename);
    },
});


const upload = multer({ 
    limits: { fileSize: 10000000 },
    storage: storage 
});

router.post("/upload", upload.single("pdfFile"), function (req, res, next) {
    const file = req.file;
    if (!file) {
        return res.status(400).send("Please upload a file");
    }
    
    // Now req.body is populated, including eid and eventname
    const { eid, eventname } = req.body;
    const originalFilePath = file.path;
    const newFilename = `${eid}_${eventname}_publicity.pdf`;
    const newFilePath = path.join("server_uploads/publicity_pdf/", newFilename);

    // Rename the file on the server file system
    fs.rename(originalFilePath, newFilePath, (err) => {
        if (err) {
            console.error('File renaming error:', err);
            return res.status(500).send("Error processing file");
        }

    // Construct the full URL to access the file via the server
    // Ensure this matches how your server is configured to serve static files
    const fullFileUrl = `${server_url}/server_uploads/publicity_pdf/${newFilename}`;


        // Proceed to insert file metadata into the database with the new filename
        const size = file.size;
        const query = "INSERT INTO publicity_files (eid, eventname, filename, filepath, size, url) VALUES (?, ?, ?, ?, ?,?)";

        connection.query(query, [eid, eventname, newFilename, newFilePath, size, fullFileUrl], function (error, results, fields) {
            if (error) {
                console.error('Database insertion error:', error);
                return res.status(500).send("Error saving file info to database");
            }
            res.send("File uploaded and renamed successfully");
        });
    });
});



router.get("/download", function (req, res) {
    const eid = req.query.eid;

    // Retrieve the file metadata from the database
    const query = "SELECT filepath, filename, url FROM publicity_files WHERE eid = ?";
    connection.query(query, [eid], function (error, results, fields) {
        if (error) return res.status(500).send("Error retrieving file from database");
        if (!results.length) return res.status(404).send("File not found");

        const { filepath, filename } = results[0];
        const absolutePath = path.join(__dirname, '..', filepath);

        // Serve the file to the client
        res.download(absolutePath, filename, function (error) {
            if (error) res.status(error.status).end();
            else console.log("Sent file:", filename);
        });
    });
});

router.get("/fetchpr", function (req, res) {
    const eid = req.query.eid;
    console.log(eid);
    // Retrieve the file metadata from the database
    const query = "SELECT filename, url FROM publicity_files WHERE eid = ?";
    connection.query(query, [eid], function (error, results, fields) {
        if (error) return res.status(500).json({ error: "Error retrieving file details from database" });
        if (!results.length) return res.status(404).json({ error: "File not found" });

        const { filename, url } = results[0];

        // Send the file details as JSON response
        res.json({ filename, url });
    });
});


router.post("/delete", function (req, res) {
    const eid = req.body.eid;

    const querySelect = "SELECT filepath, url FROM publicity_files WHERE eid = ?";
    connection.query(querySelect, [eid], function (error, results, fields) {
        if (error) return res.status(500).send("Error retrieving file for deletion: " + error.message);
        if (!results.length) return res.status(404).send("File not found for deletion");

        const absolutePath = path.join(__dirname, '..', results[0].filepath);

        fs.unlink(absolutePath, function (err) {
            if (err) return res.status(500).send("Error deleting the file: " + err.message);

            const queryDelete = "DELETE FROM publicity_files WHERE eid = ?";
            connection.query(queryDelete, [eid], function (error, results, fields) {
                if (error) return res.status(500).send("Error deleting file info from database: " + error.message);
                res.send("File deleted successfully");
            });
        });
    });
});

//Viewing Events
router.post('/viewEvent',validateSessionToken, (req, res) => {
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

router.post('/addcheckbox',validateSessionToken, (req, res) => {
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
router.post('/editPublicity',validateSessionToken,(req, res) => {
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

// Define a route to fetch uploaded files using a POST request
router.post('/fetch',validateSessionToken, (req, res) => {
    var eid = req.body.eid;
  
    // Query the database to fetch uploaded files associated with the event ID
    const query = 'SELECT filepath, filename, url FROM publicity_files WHERE eid = ?';
    
    connection.query(query, [eid], (err, results) => {
        if (err) {
            console.log('Failed to fetch files');
            res.status(500).json({ error: 'Failed to fetch files' });
        } else {
            console.log('Successfully fetched files');

            // Extract creative URLs from the database results
            const pdfUrls = results.map(result => result.url);
            console.log("pdf url for pub", pdfUrls);

            // Send the image URLs as a JSON response
            res.status(200).json({ pdfUrls });
        }
    });
});

//Fetching added checkboxes

router.get('/checkboxes',validateSessionToken, function (req, res) {
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