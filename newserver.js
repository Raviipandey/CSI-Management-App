var express=require('express');
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

//mobile app routes

var login=require('./routes/login');
var minutes=require('./routes/minutes');
var profile=require('./routes/profile');
var attendance=require('./routes/attendance');
var feedback=require("./routes/feedback");
var proposal=require('./routes/proposal');
var creative=require('./routes/creative');
var publicity=require('./routes/publicity');
var technical=require("./routes/technical");
var report=require("./routes/report.js");

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


app.get("/",(req,res)=>{
// return res.send("Welcome to CSI-DBIT");
    let session = req.session;
        if (session.userid) {
        res.redirect("/dashboard");
        } else {
            res.render(__dirname + "/views/login.ejs", {
                Emsg : req.flash("Emsg")
            });
        }
});

app.get("/logout", (req, res, next)=>{
    req.session.destroy();
    res.redirect("/");

})
//Port Listening
app.listen(9000,(req,res)=>{
    console.log("Listening on 9000");
});
