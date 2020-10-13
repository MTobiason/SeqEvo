echo ''
echo '*************************'
echo 'Creating temp directories'
echo '*************************'
echo ''

$LOCATION = Convert-Path .

function MT-CheckExitCode ($ExitText){
    if ($LastExitCode -ne 0) {
        if ($?){
            write-host "************************************"
            write-host "Warning while $ExitText" -ForegroundColor Yellow
            write-host "************************************"
        }
        if ( -not $?){
            write-host "************************************"
            write-host "Error while $ExitText" -ForegroundColor Red
            write-host "************************************"

            echo "exit status '?' = $?"
            echo "LastExitCode = $LastExitCode"
            MT-Clean            
            write-host "Script aborted: Type 'exit' to continue" -ForegroundColor Magenta
            $Host.EnterNestedPrompt()

            exit
        }
    }
    if ($LastExitCode -eq 0){
        write-host "************************************"
        write-host "Success while $ExitText" -ForegroundColor Green
        write-host "************************************"
    }

}

function MT-Clean{
    cd $LOCATION
    Remove-Item $LOCATION\MT-temp -Recurse -Force
    #MT-CheckExitCode('Failed to remove MT-temp directory')
}

New-Item "$LOCATION\MT-temp\class" -type directory -force | Out-Null
MT-CheckExitCode("creating class directory")
New-Item "$LOCATION\MT-temp\jar" -type directory -force | Out-Null
MT-CheckExitCode("creating jar directory")

if( Test-Path $Location\bin)
{
    New-Item "$LOCATION\bin" -type directory -force | Out-Null
    MT-CheckExitCode("creating bin directory")
}

$Targets = 'SeqEvo', 'DevPro'
ForEach ($Target in $Targets)
{
    cd $LOCATION\src\main\java

    echo ''
    echo '******************************'
    echo "Compiling $Target"
    echo '******************************'
    echo ''

    &javac edu\boisestate\mt\$Target.java -d $Location\MT-temp\class -source 8 -target 8

    MT-CheckExitCode("Compiling $Target")

    cd  $LOCATION\MT-temp\class

    echo ''
    echo '*************************************'
    echo "Merging $Target classes to create jar"
    echo '*************************************'
    echo ''

    &jar --create --file $LOCATION\MT-temp\jar\$Target.jar --main-class edu.boisestate.mt.$Target .\edu\boisestate\mt  
    MT-CheckExitCode("creating $Target.jar")

    echo ''
    echo '*************************************'
    echo "Moving $Target.jar to \bin folder"
    echo '*************************************'
    echo ''

    Copy-Item $LOCATION\MT-temp\jar\$Target.jar -Destination $LOCATION\bin\ -Force

    MT-CheckExitCode("copying $Target.jar to \bin folder")

    cd "$LOCATION"
}

echo ''
echo '*************************'
echo 'Removing temp directories'
echo '*************************'
echo ''
 
Remove-Item .\MT-temp -Recurse -Force

MT-CheckExitCode("removing temp directory")

echo 'Finished'
return