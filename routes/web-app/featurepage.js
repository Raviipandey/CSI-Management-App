// Assuming you have a file named dbConnect.js in the appropriate directory that exports your database connection
const connection = require("../../Database/dbConnect");
var express=require('express');
const multer = require('multer');
var app=express();
//const sharp = require('sharp');
const fs = require('fs');
const path = require('path');
const router = express.Router();
var express = require('express');




const storage = multer.diskStorage({
  destination: (req, file, cb) => {
      // Define the destination directory. Adjust the path as needed.
      const targetPath = path.join(__dirname, '..', '..', 'server_uploads', 'images_dynamic');
      // Ensure the directory exists, or create it
      fs.mkdirSync(targetPath, { recursive: true });
      cb(null, targetPath);
  },
  filename: (req, file, cb) => {
      // Generate a new filename based on event name, date, and the original file extension
      const { event_name, event_date } = req.body;
      const date = new Date(event_date);
      const formattedDate = `${date.getDate()}_${date.getMonth() + 1}_${date.getFullYear()}`;
      const newFilename = `${event_name}_${formattedDate}${path.extname(file.originalname)}`;
      cb(null, newFilename);
  }
});

// Initialize Multer with storage and file restrictions
const upload = multer({
  storage: storage,
  limits: {
    fileSize: 10 * 1024 * 1024 // 10 MB
  },
  fileFilter: (req, file, cb) => {
    if (!file.originalname.match(/\.(jpg|jpeg|png|HEIC)$/)) {
      req.fileValidationError = 'Only image files are allowed!';
      return cb(new Error('Only image files are allowed!'), false);
    }
    cb(null, true);
  }
});


router.get('/', (req, res, next) => {
    var session = req.session;
    var DashboardPath = path.join(__dirname, "..", "..", "views","pages",  "featurepage.ejs");
    console.log(session.userrole);

    if (session.userid != null) {
        res.render(DashboardPath, {role : session.userrole});
    } else {
        res.redirect('/');
    }
    // Your GET handler code
});

router.post('/', upload.single('event_image'), (req, res) => {
  if (req.fileValidationError) {
      return res.status(400).send(req.fileValidationError);
  }

  try {
      // Since the file is saved by Multer, just return a success message
      res.send('Image uploaded successfully.');
  } catch (error) {
      console.error(error);
      res.status(500).send('An error occurred.');
  }
});


module.exports = router;

  

  

 
