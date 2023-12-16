var express=require('express');
var app=express();
var bodyParser=require('body-parser');
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({
    extended: true
}));

//Making Of Directory
app.post('/mkdir',(req,res) =>{
	var address=req.body.path;
	var fname=req.body.fname;
	 // Replace spaces with underscores in the folder name
	 var sanitizedFname = fname.replace(/\s+/g, '_');

	 var directoryPath = path.join(__dirname, 'server_uploads/gallery/', address, sanitizedFname);
	
	
	fs.mkdir(directoryPath,function(err){
		if(err){
			//console.log("Error);
			res.sendStatus(400);
		}
		else{
			//console.log("Succesfully Created");
		}
	});
	directoryPath=path.join(__dirname,'server_uploads/gallery/',address);
	fs.readdir(directoryPath,function(err,files){
		if (err){
			//console.log("Error");
			res.sendStatus(400);
	    	}
		else{
			//console.log("Succesfully Listed");
			res.status(200).send(files);
		}
	});

});

//Viewing Folder In A Directory
var path = require('path');
var fs = require('fs');
app.post('/event',(req,res)=>{
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
app.post('/path',(req,res)=>{
	address=req.body.path;
	//console.log("Path Succesfully Updated");
	res.sendStatus(200);
});

//Uploading Files
app.post('/upload', (req,res) =>{
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
app.post('/view',(req,res)=>{
	var address = req.body.path;
	var link = [];
	var directoryPath = path.join(__dirname,'server_uploads/gallery/',address);
	fs.readdir(directoryPath, function (err, files){
		if (err){
			//console.log("Error");
			res.sendStatus(400);
	    	}
		else{
			for (var i in files){
        			link[i] = 'http://192.168.0.104:9091/images/'+files[i];
			}
		// console.log("Succesfully URL Sent");
		 res.status(200).send(link);
		}
	});
	app.use(express.static('public'));
	app.use('/images', express.static(__dirname + '/server_uploads/gallery/' +'/' + address));
       //app.use('/images', express.static(__dirname + '/images'));
});

// Deleting an Image
app.delete('/delete/:fileName', (req, res) => {
	const fileName = req.params.fileName;
	const imagePath = path.join(__dirname, 'server_uploads/gallery/',address, fileName);
	
	fs.unlink(imagePath, (err) => {
	  if (err) {
		// console.log("Error");
		res.sendStatus(400);
	  } else {
		// console.log("Succesfully Deleted");
		res.sendStatus(200);
	  }
	});
  });

  
app.use('/images', express.static(__dirname + '/images'));
app.use('/images', express.static(__dirname + '/creative'));
app.use('/report', express.static(__dirname + '/report'));
//port listening
app.listen(9091,(req,res)=>{
    console.log("Listening on 9091");
});


