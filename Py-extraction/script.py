from flask import Flask, request, jsonify
import os
from PyPDF2 import PdfReader
from werkzeug.utils import secure_filename
import magic
from datetime import datetime  # Pour ajouter un timestamp aux noms de fichiers

app = Flask(__name__)

# Configurations
UPLOAD_FOLDER = 'uploaded_files'
TEXT_OUTPUT_FOLDER = 'pdf_files'  # Dossier pour sauvegarder les textes extraits
os.makedirs(UPLOAD_FOLDER, exist_ok=True)
os.makedirs(TEXT_OUTPUT_FOLDER, exist_ok=True)
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER

mime = magic.Magic(mime=True)

def save_extracted_text(text, original_filename):
    """Sauvegarde le texte extrait dans un fichier .txt avec un timestamp"""
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    base_name = os.path.splitext(original_filename)[0]
    output_filename = f"{base_name}_{timestamp}.txt"
    output_path = os.path.join(TEXT_OUTPUT_FOLDER, output_filename)
    
    with open(output_path, 'w', encoding='utf-8') as f:
        f.write(text)
    
    return output_path

def extract_text_from_pdf(filepath, original_filename):
    """Extrait le texte d'un PDF et le sauvegarde"""
    text = ""
    with open(filepath, 'rb') as f:
        pdf_reader = PdfReader(f)
        for page in pdf_reader.pages:
            page_text = page.extract_text()
            if page_text:
                text += page_text + "\n"
    
    saved_path = save_extracted_text(text.strip(), original_filename)
    return text.strip(), saved_path

def extract_text_from_txt(filepath, original_filename):
    """Extrait le texte d'un fichier texte et le sauvegarde"""
    with open(filepath, 'r', encoding='utf-8') as f:
        text = f.read().strip()
    
    saved_path = save_extracted_text(text, original_filename)
    return text, saved_path

@app.route('/upload', methods=['POST'])
def handle_file_upload():
    if 'file' not in request.files:
        return jsonify({"error": "Aucun fichier dans la requête"}), 400

    file = request.files['file']

    if file.filename == '':
        return jsonify({"error": "Aucun fichier sélectionné"}), 400

    try:
        filename = secure_filename(file.filename)
        filepath = os.path.join(app.config['UPLOAD_FOLDER'], filename)
        file.save(filepath)

        file_mime_type = mime.from_file(filepath)
        result = {
            "filename": filename,
            "mime_type": file_mime_type,
            "status": "success"
        }

        if file_mime_type == 'application/pdf':
            text, saved_path = extract_text_from_pdf(filepath, filename)
            result["text"] = text
            result["saved_text_path"] = saved_path
        elif file_mime_type == 'text/plain':
            text, saved_path = extract_text_from_txt(filepath, filename)
            result["text"] = text
            result["saved_text_path"] = saved_path
        else:
            result["message"] = "Seuls les fichiers PDF et TXT sont acceptés"
            result["status"] = "unsupported"

        os.remove(filepath)
        return jsonify(result), 200

    except Exception as e:
        if 'filepath' in locals() and os.path.exists(filepath):
            os.remove(filepath)
        return jsonify({
            "error": f"Erreur lors du traitement du fichier: {str(e)}",
            "status": "error"
        }), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)