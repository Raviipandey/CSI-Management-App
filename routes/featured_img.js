var express=require('express');
const fs = require('fs');
var app=express();
const createError = require('http-errors');
var bodyParser=require('body-parser');
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({
    extended: true
}));

var cors=require('cors');
app.use(cors());
const path = require('path');
const cookieParser = require('cookie-parser');
const logger = require('morgan');
const session = require('express-session');
const oneDay = 1000 * 60 * 5;
const router = express.Router();
var flash = require("connect-flash");
app.use(flash());

const {connection , server_url} = require('../serverconfig');

const imagesDirectory = path.join(__dirname, '..', 'server_uploads', 'images_dynamic');

// Endpoint to list image URLs
router.get('/list-images', (req, res) => {
    console.log("Reached the /list-images req");
    console.log("Images Directory:", imagesDirectory);
    console.log("Server URL:", server_url);

    fs.readdir(imagesDirectory, (err, files) => {
        if (err) {
            console.error(err);
            return res.status(500).send('Server error');
        }

        // Sort files by last modification time, most recent first
        files.sort((a, b) => {
            return fs.statSync(path.join(imagesDirectory, b)).mtime.getTime() -
                   fs.statSync(path.join(imagesDirectory, a)).mtime.getTime();
        });

        // Limit to the 7 most recent files
        const recentFiles = files.slice(0, 7);

        // Generate URLs for each of the 7 most recent images
        const imageUrls = recentFiles.map(file => `${server_url}/images/${file}`);
        res.json(imageUrls);
        console.log(imageUrls);
    });
});

module.exports = router;