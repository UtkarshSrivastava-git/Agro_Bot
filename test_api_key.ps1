# Test OpenWeatherMap API Key
$apiKey = "34686d883bd40355b3519a828bd91205"
$lat = "28.6139"
$lon = "77.2090"

$url = "https://api.openweathermap.org/data/2.5/weather?lat=$lat&lon=$lon&appid=$apiKey&units=metric"

Write-Host "Testing API Key with URL:"
Write-Host $url
Write-Host ""

try {
    $response = Invoke-RestMethod -Uri $url -Method Get
    Write-Host "✅ API Key is working!"
    Write-Host "City: $($response.name)"
    Write-Host "Temperature: $($response.main.temp)°C"
    Write-Host "Weather: $($response.weather[0].description)"
} catch {
    Write-Host "❌ API Key test failed:"
    Write-Host "Status: $($_.Exception.Response.StatusCode)"
    Write-Host "Error: $($_.Exception.Message)"
    
    if ($_.Exception.Response.StatusCode -eq 401) {
        Write-Host ""
        Write-Host "This is an authentication error. Possible causes:"
        Write-Host "1. API key is invalid or expired"
        Write-Host "2. API key hasn't been activated yet (can take up to 2 hours)"
        Write-Host "3. API key has restrictions or quotas exceeded"
    }
}