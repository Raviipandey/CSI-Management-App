var express=require('express');
var app=express();
const fs = require('fs');
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

//web app session
app.use(
    session({
        resave: true,
        saveUnitialized: true,
        secret: "secret",
        cookie: { maxAge: oneDay }
    })
);

app.use(logger('dev'));
app.use(express.json());
app.use(express.urlencoded({ extended: false }));
app.use(cookieParser());
app.use(express.json({}))


//all static files are mentioned here
app.use("/views",express.static("views"));
app.use("/jquery",express.static("jquery"));

app.use(express.static(__dirname + "/views"));


app.use('/creative', express.static('./creative'));


app.use('/profile_pic', express.static('./profile_pic'));

var login=require('./routes/core_login.js');
var minutes=require('./routes/core_minutes.js');
var profile=require('./routes/core_profile.js');
var attendance=require('./routes/core_attendance.js');
var feedback=require("./routes/feedback.js");
var proposal=require('./routes/core_proposal.js');
var creative=require('./routes/core_creative.js');
var publicity=require('./routes/core_publicity.js');
var technical=require("./routes/core_technical.js");
var report=require("./routes/report.js");
const featured_img = require("./routes/featured_img.js")

app.use('/images', express.static(path.join(__dirname, 'server_uploads', 'images_dynamic')));



app.use('/login',login);
app.use('/minutes',minutes);
app.use('/profile',profile);
app.use('/attendance',attendance);
app.use('/feedback',feedback);
app.use('/proposal',proposal);
app.use('/creative',creative);
app.use('/publicity',publicity);
app.use('/technical',technical);
app.use('/report',report);
app.use('/images', featured_img);


//web app view

app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'ejs');

//web app routes

var authUser = require('./routes/web-app/authUser');
var dashboard = require('./routes/web-app/dashboard');
var proposal = require('./routes/web-app/proposal');
var feedback = require('./routes/web-app/feedback');
var minute = require('./routes/web-app/minutes');
var technical = require('./routes/web-app/technical');
var publicity = require('./routes/web-app/publicity');
var addmembers = require('./routes/web-app/addmembers')
const { request } = require('http');

app.use('/authUser', authUser.post);
app.use('/dashboard', dashboard.get);
app.use('/pydata', dashboard.pydata);
app.use('/confirmedall', dashboard.confirmall);
app.use('/proposalData', proposal.get);
app.use('/fetchall', proposal.fetchall);
app.use('/fetchsingle', proposal.fetchsingle);
app.use('/hodstatusapprove', proposal.hodstatusconf);
app.use('/hodstatusreject', proposal.hodstatusrej);
app.use('/feedbackData', feedback.get);
app.use('/feedbackall', feedback.feedbackall);
app.use('/feedbacksingle', feedback.feedbacksingle);
app.use('/feedbackupdate', feedback.feedbackupdate);
app.use('/minuteData', minute.get);
app.use('/minuteall', minute.minuteall);
app.use('/minutesingle', minute.minutesingle);
app.use('/techData', technical.get);
app.use('/techall', technical.techall);
app.use('/publicityData', publicity.get);
app.use('/publicityall', publicity.publicityall);
app.use('/addmemberspage', addmembers.get)
app.use('/addmembers', addmembers.addmembers)
app.use('/countApprovedProposals', proposal.countApprovedProposals)
app.use('/countRejectedProposals', proposal.countRejectedProposals)
app.use('/countApprovedSBCProposals', proposal.countApprovedSBCProposals)
app.use('/countRejectedSBCProposals', proposal.countRejectedSBCProposals)
app.use('/countMembers', addmembers.countMembers)



app.get("/",(req,res)=>{
    let session = req.session;
        if (session.userid) {
        res.redirect("/dashboard");
        } else {
            res.render(__dirname + "/views/pages/sign-in.ejs", {
                Emsg : req.flash("Emsg")
            });
        }
});





app.get("/logout", (req, res, next)=>{
    req.session.destroy();
    res.redirect("/");

})

// app.get("/",(req,res)=>{
// return res.send("Welcome to CSI-DBIT");
// });

//Port Listening
app.listen(9000,(req,res)=>{
    console.log("Listening on 9000");
});