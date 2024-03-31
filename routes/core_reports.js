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
        console.log('Not Connected to core Report');
    }
    else {
        console.log("Connected To core Report");
    }
});

// Create a storage object with Multer to handle file uploads

const storage = multer.diskStorage({
    destination: function (req, file, cb) {
        cb(null, "server_uploads/reports/");
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

/////----------Route to list events for report -------------//
router.get('/list',validateSessionToken,(req,res)=>{

	connection.query('SELECT * FROM core_proposals_manager',function(err,result){
		if(err){
			console.log("Report listing error");
			res.sendStatus(400);
		}
		else{
			console.log("Succesully Listed Report");
			console.log(result);
			res.status(200).send(result);
		}
	});
});

/////----------Route to upload reports -------------//
router.post("/upload", upload.single("report"), function (req, res, next) {
    const file = req.file;
    if (!file) {
        return res.status(400).send("Please upload a file");
    }
    
    const { eid, eventname } = req.body;
    const originalFilePath = file.path;
    const newFilename = `${eid}_${eventname}_report.pdf`;
    const newFilePath = path.join("server_uploads/reports/", newFilename);

    fs.rename(originalFilePath, newFilePath, (err) => {
        if (err) {
            console.error('File renaming error:', err);
            return res.status(500).send("Error processing file");
        }

    const fullFileUrl = `${server_url}/server_uploads/reports/${newFilename}`;


        const size = file.size;
        const query = "INSERT INTO reports (eid, eventname, filename, filepath, size, url) VALUES (?, ?, ?, ?, ?,?)";

        connection.query(query, [eid, eventname, newFilename, newFilePath, size, fullFileUrl], function (error, results, fields) {
            if (error) {
                console.error('Database insertion error:', error);
                return res.status(500).send("Error saving file info to database");
            }
            res.send("File uploaded and renamed successfully");
        });
    });
});

/////----------Route to fetch uploaded reports-------------//
router.get("/fetchreport", function (req, res) {
    const eid = req.query.eid;
    console.log(eid);
    // Retrieve the file metadata from the database
    const query = "SELECT filename, url FROM reports WHERE eid = ?";
    connection.query(query, [eid], function (error, results, fields) {
        if (error) return res.status(500).json({ error: "Error retrieving file details from database" });
        if (!results.length) return res.status(404).json({ error: "File not found" });

        const { filename, url } = results[0];
        res.json({ filename, url });
    });
});

/////----------Route to download reports-------------//
router.get("/download", function (req, res) {
    const eid = req.query.eid;

    // Retrieve the file metadata from the database
    const query = "SELECT filepath, filename, url FROM reports WHERE eid = ?";
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

/////----------Route to delete reports-------------//
router.post("/delete" ,function (req, res) {
    const eid = req.body.eid;

    const querySelect = "SELECT filepath, url FROM reports WHERE eid = ?";
    
    connection.query(querySelect, [eid], function (error, results, fields) {
        console.log(results);
        if (error) return res.status(500).send("Error retrieving file for deletion: " + error.message);
        if (!results.length) return res.status(404).send("File not found for deletion");

        const absolutePath = path.join(__dirname, '..', results[0].filepath);

        fs.unlink(absolutePath, function (err) {
            if (err) return res.status(500).send("Error deleting the file: " + err.message);

            const queryDelete = "DELETE FROM reports WHERE eid = ?";
            connection.query(queryDelete, [eid], function (error, results, fields) {
                if (error) return res.status(500).send("Error deleting file info from database: " + error.message);
                res.send("File deleted successfully");
            });
        });
    });
});



module.exports = router;