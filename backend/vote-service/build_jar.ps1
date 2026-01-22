$ErrorActionPreference = "Stop"

$jarScript = "d:\ide\toupiao\ROOT_CodeBuddyCN\新架构\backend\vote-service\create_jar_vote.py"
$pythonPath = "D:\ide\python\Python313\python.exe"

Write-Host "Running Python script..."
& $pythonPath $jarScript
