===============================
	Salesforce --> Google sync
	todd.newman@vistage.com
	Dreamforce 2012
===============================

1.   Create a play web app at java.heroku.com
2.   Follow command-line instructions that appear through step 3 (clone the app locally)
3.   Download zipped file from github
4.   Copy contents of zip file to your cloned app
5.   Update conf/application.conf with your google and heroku connection properties
6.   Commit and deploy your changes with
		git add .
		git commit -m 'added my conf'
7.   Push your changes to Heroku with 
		git push heroku

Note that if Google gives you a hassle about logins from multiple IPs, you can tell it everything is alright here:
https://www.google.com/dashboard/b/0/logindetails/?hl=en&pli=1
