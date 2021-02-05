    document.getElementById("linkGrabPos").onclick = function()
    {
        if (navigator.geolocation)
    {
        navigator.geolocation.getCurrentPosition(showPosition)
    }
        else
    {
        document.getElementById("wgs84Lat").value = "Ihr Browser ist veraltet und unterstützt keine Geolocation.";
    }
    }

    function showPosition(pos)
    {
        document.getElementById("wgs84Lat").innerText.value = pos.coords.latitude;
        document.getElementById("wgs84Lon").innerText.value = pos.coords.longitude;
    }
