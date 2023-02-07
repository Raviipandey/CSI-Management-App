var express=require('express');
var router=express.Router();
var dotenv = require('dotenv');
dotenv.config();

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

//Listing All events
router.get('/listcreative',(req,res)=>{
	connection.query('SELECT cpm_id,proposals_event_name,proposals_event_category,proposals_event_date FROM core_proposals_manager where proposals_status=2',function(err,result){
		if(err){
			console.log(err);
			console.log("Failed to  List All creative events");
			res.sendStatus(400);
		}
		else{
			console.log("Succesfully Listed All creative events");
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
			console.log(result[0]);
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

// set up file storage
const storage = multer.diskStorage({
    destination: (req, file, cb) => {
        cb(null, 'creative/');
    },
    filename: (req, file, cb) => {
        cb(null, file.originalname);
    }
});
const upload = multer({ storage });


router.post('/upload', upload.single('file'), (req, res) => {
    const file = req.file;
    const fileUrl = `http://localhost:9000/creative/${file.filename}`;

    // insert file information into database
    const query = `INSERT INTO core_creative_manager (creative_heading, creative_url) VALUES (?, ?)`;
    connection.query(query, [file.originalname, fileUrl], (err, results) => {
        if (err) {
            console.log(err);
            res.status(500).json({ message: 'Error uploading file' });
            return;
        }
        res.json({ message: 'File uploaded successfully' });
    });
});

module.exports=router;
