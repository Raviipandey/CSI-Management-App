var express=require('express');
var app=express();
var bodyParser=require('body-parser');
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({
    extended: true
}));

var cors=require('cors');
app.use(cors());



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

app.get("/",(req,res)=>{
return res.send("Welcome to CSI-DBIT");
});

//Port Listening
app.listen(9000,(req,res)=>{
    console.log("Listening on 9000");
});