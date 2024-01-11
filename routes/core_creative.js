var express=require('express');
var router=express.Router();
var dotenv = require('dotenv');
dotenv.config();
const fs = require('fs');
const path = require('path')
const app = express();

//Mysql Connection
var mysql = require('mysql');
var connection = mysql.createConnection({
    host: '128.199.23.207',
	user: "csi",
	password: "csi",
	database: 'csiApp2022'
});
connection.connect(function(err){
	if(err){
		console.log('Not Connected to MySql!Creative.js');
	}
	else{
		console.log("Connected To Mysql!Creative.js");
	}
});

const server_url = "http://128.199.23.207:9000"



//Listing All events
router.get('/listcreative',(req,res)=>{
	connection.query('SELECT cpm_id,proposals_event_name,proposals_event_category,proposals_event_date FROM core_proposals_manager where proposals_status=3',function(err,result){
		if(err){
			console.log(err);
			console.log("Failed to  List All creative events");
			res.sendStatus(400);
		}
		else{
			console.log("Succesfully Listed All creative events");
			console.log(result);
			res.status(200).send(result);
		}
	});
});

//Viewing pre-filled event detail
router.post('/viewpropdetail',(req,res)=>{
	var cpm_id=req.body.cpm_id;
	console.log(cpm_id);
	var qry = 'SELECT * FROM(SELECT core_creative_manager.cpm_id,proposals_event_name,proposals_event_category,proposals_event_date,speaker, proposals_venue , proposals_reg_fee_csi ,proposals_reg_fee_noncsi ,proposals_prize , proposals_desc , proposals_creative_budget, proposals_publicity_budget, proposals_guest_budget , creative_url FROM core_proposals_manager,core_creative_manager WHERE core_proposals_manager.cpm_id=core_creative_manager.cpm_id) AS creative WHERE cpm_id=?'
	connection.query(qry,[cpm_id],function(err,result){
		if(err){
			console.log("Failed to view  creative event detail");
			res.sendStatus(400);
		}
		else{
			console.log("Succesfully viewed creative event detail");
			console.log(result);
			res.status(200).send(result[0]);
		}
	});
});

//Submit Creative
router.post('/submit',(req,res)=>{
	var eid = req.body.eid;
	var poster = req.body.poster;
	var video = req.body.video;

	connection.query("UPDATE creative SET poster_link=?,video_link=?,status=3 WHERE eid=?",[poster,video,eid],function(error){
		if(error){
			console.log("Submit Creative Error");
			res.sendStatus(400);
		}
		else{
			console.log("Creative Form Successfully Submitted");
			res.sendStatus(200);
		}
	});
});


//Poster,video uploading
var multer=require('multer');

// var storage=multer.diskStorage({
// 	destination:function(req,file,cb){
// 		cb(null,'creative/');
// 	},
// 	filename:function(req,file,cb){
// 		cb(null,file.originalname);
// 	}
// });

// var upload=multer({
// 	storage:storage,
// 	limits:{
// 		fileSize: 1024*1024 *10
// 	}
// }).array('file',3);

// router.post('/upload',(req,res)=>{
//    	upload(req,res,function(err){
// 		if(err){
// 			console.log("Upload Error");
// 			res.sendStatus(400);
// 		}
// 		else{
// 			console.log("Succesfully Uploaded");
// 			res.sendStatus(200);
// 		}
// 	});
// });



// Set up file storage
const storage = multer.diskStorage({
    destination: (req, file, cb) => {
        cb(null, 'creative/');
    },
    filename: (req, file, cb) => {
        const eid = req.body.eid;
        const fileheader = req.body.fileheader;

        // Fetch data from the database and update filename
        connection.query('SELECT proposals_event_name FROM core_proposals_manager WHERE cpm_id = ?', [eid], (err, results) => {
            if (err) {
                console.log(err);
                return cb(err);
            }

            const proposalName = results[0].proposals_event_name;
            // Ensure the filename is URL-friendly
            const sanitizedFileHeader = fileheader.replace(/\s/g, ''); // Remove spaces
            const baseFilename = `${proposalName}_${sanitizedFileHeader}${path.extname(file.originalname)}`;
            
            // Check if the file already exists
            let filename = baseFilename;
            let counter = 1;

            while (fs.existsSync(path.join('creative/', filename))) {
                filename = `${baseFilename}_${counter}${path.extname(file.originalname)}`;
                counter++;
            }

            cb(null, filename);
        });
    }
});
const upload = multer({ storage });

router.post('/upload', upload.single('file'), (req, res) => {
	const eid = req.body.eid;
	const fileheader = req.body.fileheader;
	const file = req.file;
	
	// Ensure the filename is URL-friendly
	const sanitizedFileHeader = fileheader.replace(/\s/g, ''); // Remove spaces
	const fileUrl = `${server_url}/creative/${file.filename}`;
  
	// Insert file information into the database
	connection.query('SELECT proposals_event_name FROM core_proposals_manager WHERE cpm_id = ?', [eid], (err, results) => {
	  if (err) {
		console.log(err);
		res.status(500).json({ message: 'Error uploading file' });
		return;
	  }
  
	  const proposalName = results[0].proposals_event_name;
	  const filename = `${proposalName}_${sanitizedFileHeader}${path.extname(file.originalname)}`;
  
	  const query = `INSERT INTO core_creative_manager (creative_heading, creative_url, cpm_id) VALUES (?, ?, ?)`;
	  connection.query(query, [filename, fileUrl, eid], (err, results) => {
		if (err) {
		  console.log(err);
		  res.status(500).json({ message: 'Error uploading file' });
		  return;
		}
		res.json({ message: 'File uploaded successfully' });
	  });
	});
  });

// Define a route to fetch uploaded files using a POST request
router.post('/fetch', (req, res) => {
    var eid = req.body.eid;
  
    // Query the database to fetch uploaded files associated with the event ID
    const query = 'SELECT creative_heading, creative_url FROM core_creative_manager WHERE cpm_id = ?';
    
    connection.query(query, [eid], (err, results) => {
        if (err) {
            console.log('Failed to fetch files');
            res.status(500).json({ error: 'Failed to fetch files' });
        } else {
            console.log('Successfully fetched files');

            // Extract creative URLs from the database results
            const imageUrls = results.map(result => result.creative_url);

            // Send the image URLs as a JSON response
            res.status(200).json({ imageUrls });
        }
    });
});
  
  router.get('/fetch/:filename', (req, res) => {
	const filename = req.params.filename;
	const file = path.join(__dirname, 'creative', filename);
  
	// Check if the file exists
	if (fs.existsSync(file)) {
	  // Send the file as a response
	  res.sendFile(file);
	} else {
	  // File not found
	  res.status(404).json({ message: 'File not found' });
	}
  });
  
module.exports=router;