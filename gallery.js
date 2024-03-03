var express=require('express');
var app=express();
var bodyParser=require('body-parser');
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({
    extended: true
}));
var router = express.Router();
router.use(bodyParser.json());
router.use(bodyParser.urlencoded({
    extended: true
}));

//Making Of Directory
router.post('/mkdir', (req, res) => {
    console.log("Inside /mkdir");
    var address = req.body.path;
    var fname = req.body.fname;

    // Keep the folder name as is, including spaces
    var directoryPath = path.join(__dirname, 'server_uploads/gallery/', address, fname);

    fs.mkdir(directoryPath, function(err) {
        if (err) {
            console.log("Error creating directory: ", err);
            res.sendStatus(400);
            return; // Make sure to return here so the rest of the code doesn't execute
        }
    });

    // Re-read the directory list
    directoryPath = path.join(__dirname, 'server_uploads/gallery/', address);
    fs.readdir(directoryPath, function(err, files) {
        if (err) {
            console.log("Error reading directory: ", err);
            res.sendStatus(400);
        } else {
            res.status(200).send(files);
        }
    });
});

//Viewing Folder In A Directory
var path = require('path');
var fs = require('fs');
router.post('/event',(req,res)=>{
	console.log("This is the path" , req.body.path);
	var address=req.body.path;
	var directoryPath=path.join(__dirname,'server_uploads/gallery/',address);
	fs.readdir(directoryPath,function(err,files){
		if(err){
			//console.log("Error");
			res.sendStatus(400);
	    	}
		else{
			//console.log("Succesfully Listed");
			res.status(200).send(files);
		}
	});
});

//Uploading Images and Videos
var multer=require('multer');
const { server_url } = require('./serverconfig');
var address;
var storage=multer.diskStorage({
	destination: function(req,file,cb){
		cb(null,'server_uploads/gallery/'+address);
	},
	filename: function(req,file,cb){
		cb(null,file.originalname);
	}
});
var upload=multer({
	storage:storage
}).array('file',10);

//Updating Path
router.post('/path',(req,res)=>{
	address=req.body.path;
	//console.log("Path Succesfully Updated");
	res.sendStatus(200);
});

//Uploading Files
router.post('/upload', (req,res) =>{
   	upload(req,res,function(err){
		if(err){
			//console.log("Error");
			res.sendStatus(400);
		}
		else{
			//console.log("Succesfully Uploaded");
			res.sendStatus(200);
		}
	});
});

//Viewing Images In A Directory
router.post('/view',(req,res)=>{
	var address = req.body.path;
	console.log("This is the address", address);
	var link = [];
	var directoryPath = path.join(__dirname,'server_uploads/gallery/',address);
	console.log(directoryPath);
	fs.readdir(directoryPath, function (err, files){
		if (err){
			//console.log("Error");
			res.sendStatus(400);
	    	}
		else{
			for (var i in files){
        			link[i] = server_url+'/galleryimages/' + address + files[i];
			}
		// console.log("Succesfully URL Sent");
		 res.status(200).send(link);
		}
	});
	router.use(express.static('public'));
	// app.use('/galleryimages', express.static(path.join(__dirname, 'server_uploads', 'gallery')));
       //app.use('/images', express.static(__dirname + '/images'));
});

router.delete('/delete/:year/:folder/:fileName', (req, res) => {
    const { year, folder, fileName } = req.params;

    const filePath = path.join(__dirname, 'server_uploads/gallery', year, folder, fileName);

    // Check if the file exists
    if (fs.existsSync(filePath)) {
        // Delete the file
        fs.unlink(filePath, (err) => {
            if (err) {
                console.error('Error deleting image:', err);
                res.sendStatus(400);
            } else {
                console.log('Successfully deleted');
                res.sendStatus(200);
            }
        });
    } else {
        console.error('File not found');
        res.sendStatus(404);
    }
});

app.use('/galleryimages', express.static(__dirname + '/galleryimages'));
app.use('/images', express.static(__dirname + '/creative'));
app.use('/report', express.static(__dirname + '/report'));
//port listening
// app.listen(9091,(req,res)=>{
//     console.log("Listening on 9091");
// });
module.exports = router;