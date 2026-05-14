async function runInference() {
    const project = document.getElementById("project").value;
    const file = document.getElementById("image").files[0];

    if (!file) {
        alert("Select image first");
        return;
    }

    const formData = new FormData();
    formData.append("roboflowProject", project);
    formData.append("imageFile", file);

    const res = await fetch("/infer", {
        method: "POST",
        body: formData
    });

    const data = await res.json();

    if (data.error) {
        alert(data.error);
        return;
    }

    const img = document.getElementById("img");
    const canvas = document.getElementById("canvas");
    const ctx = canvas.getContext("2d");

    // 1. load image locally (faster + no backend base64 needed)
    const reader = new FileReader();

    reader.onload = function (e) {
        img.src = e.target.result;

        img.onload = function () {
           
            canvas.width = img.width;
            canvas.height = img.height;

            
            ctx.drawImage(img, 0, 0);

            
            data.detections.forEach(d => {
               
                const centerX = d.x;
                const centerY = d.y;
                const width = d.width;
                const height = d.height;

             
                const topLeftX = centerX - width / 2;
                const topLeftY = centerY - height / 2;

                ctx.strokeStyle = "red";
                ctx.lineWidth = 2;
                ctx.strokeRect(topLeftX, topLeftY, width, height);

                ctx.fillStyle = "red";
                ctx.font = "14px Arial";
                const confidencePercent = (d.confidence * 100).toFixed(1);
                const label = `${d.clazz} ${confidencePercent}%`;
                ctx.fillText(label, topLeftX, topLeftY - 5);
            });

        
            downloadImage(canvas);
        };
    };

    reader.readAsDataURL(file);

    
    downloadJSON(data);
}


function downloadJSON(data) {
    const blob = new Blob([JSON.stringify(data, null, 2)], { type: "application/json" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = "inference-result.json";
    a.click();
    URL.revokeObjectURL(url);
}


function downloadImage(canvas) {
    const link = document.createElement("a");
    link.download = "annotated-image.png";
    link.href = canvas.toDataURL("image/png");
    link.click();
}


async function uploadImage() {
    const projectId = document.getElementById("projectId").value;
    const file = document.getElementById("fileInput").files[0];
    const formData = new FormData();
    formData.append("file", file);
    const res = await fetch(`/projects/${projectId}/images`, {
        method: "POST",
        body: formData
    });
    const data = await res.json();
    alert("Uploaded!");
    loadImages();
}

async function loadImages() {
    const projectId = document.getElementById("projectId").value;
    const res = await fetch(`/projects/${projectId}/images`);
    const data = await res.json();
    const container = document.getElementById("imageList");
    container.innerHTML = "";
    data.images.forEach(img => {
        const div = document.createElement("div");
        const status = img.annotated ? "ANNOTATED" : "NOT ANNOTATED";
        div.innerHTML = `
            <img src="${img.url}" width="150"/>
            <p>Status: ${status}</p>
            <a href="${img.annotation_url}" target="_blank">Open Annotator</a>
        `;
        container.appendChild(div);
    });
}