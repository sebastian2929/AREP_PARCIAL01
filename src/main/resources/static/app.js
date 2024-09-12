function compute() {
    var input = document.getElementById('input').value;
    fetch('/computar?comando=' + encodeURIComponent(input))
        .then(response => response.json())
        .then(data => document.getElementById('result').innerHTML = 'Result: ' + data.result)
        .catch(error => document.getElementById('result').innerHTML = 'Error: ' + error.message);
}