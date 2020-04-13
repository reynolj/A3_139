#!/bin/bash

#TESTDIR override (used by graders)
TESTDIR=insert/path/to/the/directory/with/your/tests/here

at139help()
{
	echo -e "TO TEST YOUR CODE RUN THE FOLLOWING TESTING COMMANDS \e[31mIN THE SAME DIRECTORY THAT YOU HAVE WRITTEN YOUR CODE IN\e[m:"
	echo "Command descriptions:"
	echo -e "\e[31mjtest\e[m: compiles your \e[31mJAVA\e[m implementation of the assignment and runs all the tests on it"
	echo -e "\e[32mctest\e[m: compiles your \e[32mC\e[m implementation of the assignment and runs all the tests on it"
	echo -e "\e[36mcpptest\e[m: compiles your \e[36mC++\e[m implementation of the assignment and runs all the tests on it"
	echo "runalltests (java|c|cpp): DOES NOT COMPILE YOUR CODE. You must pass in the language you wrote your code in. Runs all tests on your code"
	echo "run1test (java|c|cpp) <testpath>: DOES NOT COMPILE YOUR CODE. You must pass in the language you wrote your code in. Runs the test at testpath."
}

#conffile
AUTOCONFDIR=~/.autotest139
CONFFILE=$AUTOCONFDIR/testsconf
NOAUTOCONF=$AUTOCONFDIR/disableac	#the option disableac is meant for graders
TF=a3	#test folder under $AUTOCONFDIR

if [ -e $CONFFILE ]
then
	if [ -d $(cat $CONFFILE) ]
	then
		TESTDIR=$(cat $CONFFILE)
	fi
fi

#check for blacklisted file(s)
#used to tell if we need to automatically re-initialize the TESTDIR
blacklistcheck()
{
	local blacklist=(
		"9a638811eeb983b227c835cb621e8440"
		"e0294a03fa465c6644b8414fa5e38a67"
	)

	if [ ! -d $AUTOCONFDIR/$TF ]
	then
		return 0
	fi

	for f in $AUTOCONFDIR/$TF/*;
	do
		for blackhash in ${blacklist[@]};
		do
			if [ `md5sum $f | cut -d ' ' -f 1` == $blackhash ];
			then
				return 1
			fi
		done
	done

	return 0
}

#automatically configures the users testdir
#takes one argument the name of this file
autoconf()
{
	# setup autoconf dir
	if [ ! -e $AUTOCONFDIR ]
	then
		mkdir $AUTOCONFDIR
		echo "Making autotest folder at '$AUTOCONFDIR'"
	elif [ -d $AUTOCONFDIR ]
	then
		echo "'$AUTOCONFDIR' already exists"
	else
		echo "Failed to default configure autotest. A file already exists at '$AUTOCONFDIR'"
		exit 1
	fi
	#setup default assignment TF tests dir
	if [ ! -e $AUTOCONFDIR/$TF ]
	then
		mkdir $AUTOCONFDIR/$TF
		echo "Making testing folder at '$AUTOCONFDIR/$TF'"
	elif [ -d $AUTOCONFDIR/$TF ]
	then
		echo "'$AUTOCONFDIR/$TF' already exists"
	else
		echo "Failed to default configure autotest. A file already exists at '$AUTOCONFDIR/$TF'. Aborting."
		exit 1
	fi

	#copy in tests
	atd=$AUTOCONFDIR/$TF
	testNa=(
		input1.txt  output1.txt  input2.txt  output2.txt  input3.txt  output3.txt  input4.txt  output4.txt  input5.txt  output5.txt
		input6.txt  output6.txt  input7.txt  output7.txt  input8.txt  output8.txt  input9.txt  output9.txt  input10.txt output10.txt
		input11.txt output11.txt input12.txt output12.txt input13.txt output13.txt input14.txt output14.txt input15.txt output15.txt
		input16.txt output16.txt
	)

	testVal=(
		"RR 3\n3\n1 0 24 1\n2 0 3 1\n3 0 3 1\n"
		"RR 3\n0\t1\n3\t2\n6\t3\n9\t1\n12\t1\n15\t1\n18\t1\n21\t1\n24\t1\n27\t1\nAVG Waiting Time: 5.00\n"
		"SJF\n4\n1 0 6 1\n2 0 8 1\n3 0 7 1\n4 0 3 1\n"
		"SJF\n0\t4\n3\t1\n9\t3\n16\t2\nAVG Waiting Time: 7.00\n"
		"SJF\n4\n1 0 4 1\n2 2 5 1\n3 3 5 1\n4 6 3 1\n"
		"SJF\n0\t1\n4\t2\n9\t4\n12\t3\nAVG Waiting Time: 3.50\n"
		"PR_noPREMP\n5\n1 0 10 3\n2 0 1 1\n3 0 2 4\n4 0 1 5\n5 0 5 2\n"
		"PR_noPREMP\n0\t2\n1\t5\n6\t1\n16\t3\n18\t4\nAVG Waiting Time: 8.20\n"
		"PR_noPREMP\n4\n1 1 4 3\n2 3 1 1\n3 4 2 4\n4 0 1 2\n"
		"PR_noPREMP\n0\t4\n1\t1\n5\t2\n6\t3\nAVG Waiting Time: 1.00\n"
		"PR_withPREMP\n4\n1 0 8 3\n2 3 1 1\n3 5 2 4\n4 6 2 2\n"
		"PR_withPREMP\n0\t1\n3\t2\n4\t1\n6\t4\n8\t1\n11\t3\nAVG Waiting Time: 2.25\n"
		"PR_withPREMP\n4\n1 1 4 3\n2 3 1 1\n3 4 2 4\n4 0 1 2\n"
		"PR_withPREMP\n0\t4\n1\t1\n3\t2\n4\t1\n6\t3\nAVG Waiting Time: 0.75\n"
		"PR_withPREMP \n3\n1 0 8 3\n2 3 1 1\n3 5 2 4\n"
		"PR_withPREMP\n0\t1\n3\t2\n4\t1\n9\t3\nAVG Waiting Time: 1.67\n"
		"PR_noPREMP\n3\n1 0 8 3\n2 3 1 1\n3 5 2 4\n"
		"PR_noPREMP\n0\t1\n8\t2\n9\t3\nAVG Waiting Time: 3.00\n"
		"RR 4\n3\n1 0 24 1\n2 2 3 1\n3 4 6 1\n"
		"RR 4\n0\t1\n4\t2\n7\t3\n11\t1\n15\t3\n17\t1\n21\t1\n25\t1\n29\t1\nAVG Waiting Time: 6.00\n"
		"RR 3\n4\n1 0 24 1\n2 6 3 1\n3 12 6 1\n4 18 4 1\n"
		"RR 3\n0\t1\n3\t1\n6\t2\n9\t1\n12\t3\n15\t1\n18\t3\n21\t4\n24\t1\n27\t4\n28\t1\n31\t1\n34\t1\nAVG Waiting Time: 5.50\n"
		"SJF\n4\n1 0 2 1\n2 0 4 1\n3 0 6 1\n4 0 8 1\n"
		"SJF\n0\t1\n2\t2\n6\t3\n12\t4\nAVG Waiting Time: 5.00\n"
		"SJF\n4\n1 0 8 1\n2 3 4 1\n3 3 7 1\n4 6 3 1\n"
		"SJF\n0\t1\n8\t4\n11\t2\n15\t3\nAVG Waiting Time: 5.50\n"
		"PR_noPREMP\n5\n1 0 10 3\n2 0 3 1\n3 0 3 4\n4 0 1 5\n5 0 5 2\n"
		"PR_noPREMP\n0\t2\n3\t5\n8\t1\n18\t3\n21\t4\nAVG Waiting Time: 10.00\n"
		"RR 3\n15\n1 0 24 1\n2 3 3 1\n3 6 5 1\n4 9 6 1\n5 9 7 1\n6 10 4 1\n7 13 4 1\n8 16 4 1\n9 20 5 1\n10 21 10 1\n11 25 1 1\n12 30 3 1\n13 34 4 1\n14 36 7 1\n15 40 4 1\n"
		"RR 3\n0\t1\n3\t2\n6\t1\n9\t3\n12\t4\n15\t5\n18\t1\n21\t6\n24\t3\n26\t7\n29\t4\n32\t8\n35\t5\n38\t9\n41\t10\n44\t1\n47\t6\n48\t11\n49\t7\n50\t12\n53\t13\n56\t8\n57\t14\n60\t5\n61\t15\n64\t9\n66\t10\n69\t1\n72\t13\n73\t14\n76\t15\n77\t10\n80\t1\n83\t14\n84\t10\n85\t1\n88\t1\nAVG Waiting Time: 33.00\n"
		"PR_withPREMP \n15\n1 0 8 3\n2 3 1 4\n3 5 2 12\n4 5 2 13\n5 6 2 13\n6 8 2 8\n7 9 2 3\n8 14 2 4\n9 16 2 5\n10 20 2 5\n11 23 2 1\n12 24 2 2\n13 27 2 3\n14 30 2 2\n15 33 2 15\n"
		"PR_withPREMP\n0\t1\n8\t2\n9\t7\n11\t6\n13\t3\n14\t8\n16\t9\n18\t3\n19\t4\n20\t10\n22\t4\n23\t11\n25\t12\n27\t13\n29\t5\n30\t14\n32\t5\n33\t15\nAVG Waiting Time: 4.13\n"
	)

	#copying this file to autotest directory
	echo "Copying this file to $AUTOCONFDIR/$1"
	cp $1 $AUTOCONFDIR

	echo "Creating tests"
	for i in ${!testNa[@]}
	do
		if [ -e $atd/${testNa[i]} ]
		then
			rm -rf $atd/${testNa[i]}
		fi
		printf "${testVal[i]}" > $atd/${testNa[i]}
	done

	#if no testconf file exists set one up
	if [ ! -e $CONFFILE ]
	then
		echo "$AUTOCONFDIR/$TF" > $CONFFILE
		echo "No testconf file found. Setting '$AUTOCONFDIR/$TF' as default tests directory"
	elif [ -d $(cat $CONFFILE) ]
	then
		echo "$AUTOCONFDIR/$TF" > $CONFFILE
		echo "testconf already exists.  It sets TESTDIR to '$TESTDIR'."
		echo "Setting '$AUTOCONFDIR/$TF' as default tests directory"
	else
		echo "ERROR: testconf file '$CONFFILE' already exists and tries to set TESTDIR to illegal path.  Edit it to designate a legal path."
		exit 1
	fi
}

blacklistcheck
BLC=$?

#for when the script is run
if [ "$BASH_SOURCE" = $0 ]
then

	if [ "$1" = "defaultconf" ]
	then
		autoconf $0

		#autoconf complete
		exit 0
	fi

	echo -e "\e[31mYOU ARE TRYING TO EXECUTE THIS SCRIPT\e[m.  To use the testing commands source it instead."
	echo -e "Try running '\e[36msource $BASH_SOURCE\e[m' or '\e[36m. $BASH_SOURCE\e[m' (make sure to include the space between the period and $BASH_SOURCE)"
	echo "Your test directory is: $TESTDIR"
	echo ""
#	echo -e "If you want to automatically configure this script run '\e[36m$0 defaultconf\e[m' (recommended)"
#	echo "Alternatively, you can edit the variable TESTDIR at the top of this file '$0' to store an ABSOLUTE PATH to your testdir"
#	echo ""

	if [ $BLC = 1 ] && [ -e $NOAUTOCONF ]
	then
		echo -e "Your TESTDIR contains \e[31mOUTDATED ERRONEOUS TESTS\e[m."
		echo "Because you disabled automatic test initialization the tests were not re-initialized"
		echo -e "To fix your default tests run the command '\e[36m$0 defaultconf\e[m'"
		echo ""
	elif [ $BLC = 1 ]
	then
		echo -e "\e[31mOutdated tests\e[m were found in your auto-configured TESTDIR.  \e[36mRe-initializing.\e[m"
		autoconf $0
		exit 0
	else
		echo -e "Test auto-configuration folder: \e[32mOK\e[m (contains no known buggy tests as of 4/7/2020)"
		echo ""
	fi

	exit 1
fi

if [  ! -e $NOAUTOCONF ] && [ $TESTDIR != $AUTOCONFDIR/$TF ]
then
	echo "Your TESTDIR has not been set up for this assignment yet. Configuring now."
	autoconf $BASH_SOURCE
	TESTDIR=$AUTOCONFDIR/$TF
elif [ $BLC = 1 ] && [ ! -e $NOAUTOCONF ]
then
	echo -e "Outdated tests were found in your auto-configured TESTDIR.  \e[32mFixing Problem\e[m."
	autoconf $BASH_SOURCE
fi

if [ ! -d $TESTDIR ]
then
	echo "Your TESTDIR appears to not be configured."
	echo -e "Either run '\e[36m$BASH_SOURCE defaultconf\e[m' (recommended)"
	echo "Or alternatively, Edit the variable TESTDIR at the top of the file $BASH_SOURCE to be the location of the test cases you got from canvas"
	echo "TESTDIR is set to '$TESTDIR'. No such directory exists."
	echo "MAKE SURE TO RE-SOURCE $BASH_SOURCE AFTER YOU CHANGE TESTDIR"
	echo ""
else
	echo "Using TESTDIR: $TESTDIR"
	echo ""
	at139help

	if ! [ "${PS1%%enabled*}" == "(autotest139: " ]
	then
		PS1="(autotest139: enabled) $PS1"
	fi
fi

jtest()
{
	javac *.java
	if [ $? != 0 ]
	then
		echo "error did not compile"
		return
	fi

	runalltests java
}

ctest()
{
	gcc -std=c99 *.c -o program_under_test
	if [ $? != 0 ]
	then
		echo "error did not compile"
		return
	fi

	runalltests c
}

cpptest()
{
	g++ *.cpp -o program_under_test
	if [ $? != 0 ]
	then
		echo "error did not compile"
		return
	fi

	runalltests cpp
}

runalltests()
{
	if [ ! -d $TESTDIR ]
	then
		echo "Your TESTDIR appears to be misconfigured. Edit the variable TESTDIR at the top of the autotest file you sourced to be the location of the test cases you got from canvas"
		echo "TESTDIR is set to '$TESTDIR'. No such directory exists."
		echo ""
		return
	fi

	for f in $TESTDIR/*
	do
		if [ -f "${f%input*}output${f##*input}" ]
		then
			run1test $1 $f
		fi
	done
}

run1test()
{
	#get the command
	local runcom
	if [ $1 = "java" ]
	then
		local mf=`grep -rEl "public\s*static\s*void\s*main"`
		if [ `grep -rE "public\s*static\s*void\s*main" | wc -l` != 1 ]
		then
			echo "could not find main class/found too many main classes"
			return 1
		fi
		runcom="java ${mf%.java}"
	elif [ $1 = "c" ] || [ $1 = "cpp" ]
	then
		runcom="./program_under_test"
	else
		echo "Did not recognize language"
	fi

	#check if there are proper input and output files
	local of="${2%input*}output${2##*input}"
	if [ -f $2 ] || [ -f $of ]
	then
		echo -e "Testing the program with input file '\e[36m$2\e[m' and test output '\e[36m$of\e[m' and runcom '\e[36m$runcom\e[m'"
		if [ -f input.txt ]
		then
			rm input.txt
		fi
		ln -s $2 input.txt
		$runcom
		diff -i output.txt $of
		if [ $? = 0 ]
		then
			echo -e "\e[32mTest passed\e[m"
		fi
	else
		echo -e "No input and test output pair '$2' and '\e[31m$of\e[m'"
	fi
}
