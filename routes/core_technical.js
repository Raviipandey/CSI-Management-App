var express = require('express');
var router = express.Router();
var mysql = require('mysql');
const bodyParser = require("body-parser");
const app = express();
app.use(bodyParser.json());
const multer = require('multer');
const path = require('path');
const fs = require('fs');
const request = require('request');
const { server_url} = require('../serverconfig');

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
connection.connect(function(err) {
    if (!err) {
       console.log('Connected to MySql!Technical.js');
    } else {
        console.log('Not Connected to MySql!Technical.js');
    }
});


// Create a storage object with Multer to handle file uploads

const storage = multer.diskStorage({
    destination: function (req, file, cb) {
        cb(null, "server_uploads/technical_pdf/");
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
    console.log("name", eventname);
    const originalFilePath = file.path;
    const newFilename = `${eid}_${eventname}_technical.pdf`;
    const newFilePath = path.join("server_uploads/technical_pdf/", newFilename);

    // Rename the file on the server file system
    fs.rename(originalFilePath, newFilePath, (err) => {
        if (err) {
            console.error('File renaming error:', err);
            return res.status(500).send("Error processing file");
        }


    // Construct the full URL to access the file via the server
    // Ensure this matches how your server is configured to serve static files
    const fullFileUrl = `${server_url}/server_uploads/technical_pdf/${newFilename}`;

        // Proceed to insert file metadata into the database with the new filename
        const size = file.size;
        const query = "INSERT INTO technical_files (eid, eventname, filename, filepath, size, url) VALUES (?, ?, ?, ?, ?, ?)";

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
    const query = "SELECT filepath, filename, url FROM technical_files WHERE eid = ?";
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

router.get("/fetchtech", function (req, res) {
    const eid = req.query.eid;
    console.log(eid);
    // Retrieve the file metadata from the database
    const query = "SELECT filename, url FROM technical_files WHERE eid = ?";
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

    const querySelect = "SELECT filepath, url FROM technical_files WHERE eid = ?";
    connection.query(querySelect, [eid], function (error, results, fields) {
        if (error) return res.status(500).send("Error retrieving file for deletion: " + error.message);
        if (!results.length) return res.status(404).send("File not found for deletion");

        const absolutePath = path.join(__dirname, '..', results[0].filepath);

        fs.unlink(absolutePath, function (err) {
            if (err) return res.status(500).send("Error deleting the file: " + err.message);

            const queryDelete = "DELETE FROM technical_files WHERE eid = ?";
            connection.query(queryDelete, [eid], function (error, results, fields) {
                if (error) return res.status(500).send("Error deleting file info from database: " + error.message);
                res.send("File deleted successfully");
            });
        });
    });
});


//Viewing Events
router.post('/viewEvents', (req, res) =>{
    var eid = req.body.eid;
    var checkboxStatus = req.body.checkboxStatus;

	const sql = "SELECT e.proposals_event_name, e.proposals_event_category, tech_comment, e.proposals_event_date, e.speaker, e.proposals_venue, e.proposals_reg_fee_csi, e.proposals_reg_fee_noncsi, e.proposals_prize, e.proposals_desc, e.proposals_creative_budget, e.proposals_publicity_budget, e.proposals_guest_budget, t.qs_set, t.internet, t.tech_comment, t.software_install, tt.tasks, tt.status FROM core_proposals_manager e INNER JOIN core_technical_manager t ON e.cpm_id = t.cpm_id LEFT JOIN ( SELECT cpm_id, GROUP_CONCAT(task) AS tasks, GROUP_CONCAT(status) AS status FROM technical_tasks GROUP BY cpm_id ) tt ON t.cpm_id = tt.cpm_id WHERE t.cpm_id = ?"
    connection.query(sql, [eid], function (error, results) {
        if (error){
            console.log("Failed To view Technical events");
            res.sendStatus(400);
        }
        else
        {
            console.log("Successfully viewed Technical events");
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

router.post('/addcheckbox',(req,res)=>{
    var eid = req.body.eid;
    var qs_set = req.body.qs_set;    
    var internet = req.body.internet;    
    var comment = req.body.comment;
    var software_install = req.body.software_install;    
    var checkedCheckboxes = req.body.checkedCheckboxes;
    console.log(checkedCheckboxes);
    var checkboxStatus = req.body.checkboxStatus;
    lenghtofarray = Object.keys(checkedCheckboxes || {}).length
    // Store the checkedCheckboxes list in the MySQL database
    for (let i = 0; i < lenghtofarray; i++) {
        
        const checked = 1;
        const checkbox = checkedCheckboxes[i];
        const boxstatus = checkboxStatus[i];
        console.log(boxstatus);

        // Check if the checkbox is already present in the table
        connection.query("SELECT * FROM technical_tasks WHERE task=? AND cpm_id=?", [checkbox, eid], (error, results) => {
            if (error) {
                console.error("Error aa raha hai" , error);
                res.sendStatus(500);
                return;
            } else {
                if (results.length > 0) {
                    console.log("Checkbox already present");
                    return;
                } else {
                    connection.query("INSERT INTO technical_tasks (task, status , cpm_id) VALUES (?, ? , ?)", [checkbox, boxstatus , eid], (error, results) => {
                        if (error) {
                            console.error("Error aa raha hai" , error);
                            res.sendStatus(500);
                            return;
                        } else {
                            console.log("Ho gai entryy" ,results);
                        }
                    });
                }
            }
        });
    }
});

// router.get('/checkbox-data', (req, res) => {
//     const sql = "SELECT cpm_id, task FROM technical_tasks"; // your SQL query to retrieve the checkbox data
//     connection.query(sql, (err, result) => {
// 		console.log(result);
//         if (err) throw err;
//         res.json(result); // return the data as JSON
//     });
// });


//Edit Technical
router.post('/editEvents' , (req,res)=>{
	var eid = req.body.eid;
	var qs_set = req.body.qs_set;	
	var internet = req.body.internet;	
	var comment = req.body.comment;
	var software_install = req.body.software_install;

	connection.query('UPDATE core_technical_manager SET qs_set=?, internet=?, software_install=?,tech_comment=?,tech_req_status=3 WHERE cpm_id=?',[qs_set, internet,software_install,comment,eid],function(err, result){
		if(err){
				console.log("Failed to update Technical Event ");			
				res.sendStatus(400);
			}
		else{
					console.log("Sucessfully updated technical event");
					console.log(result);
					res.sendStatus(200);
			}
		});
});


//Fetching added checkboxes

router.get('/checkboxes', function(req, res) {
    const status = 1;
    const sql = 'SELECT task FROM `technical_tasks` WHERE status = 1';
    connection.query(sql, [status], function(error, results, fields) {
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