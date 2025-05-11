import os
import re
import requests
import traceback
from flask import Flask, request, jsonify
from dotenv import load_dotenv

# Load environment variables (e.g., HF_API_TOKEN)
load_dotenv()

API_URL = "https://router.huggingface.co/novita/v3/openai/chat/completions"
MODEL = "deepseek/deepseek-v3-0324"
API_KEY = os.getenv("HF_API_TOKEN")

app = Flask(__name__)

def fetch_quiz_from_model(topic):
    prompt = f"""
Generate 6 multiple-choice quiz questions about {topic}.
Use **Question:** and **Answer:** to clearly label each item.
Each question should follow this format:

**Question:** What is...
A. Option 1
B. Option 2
C. Option 3
D. Option 4
**Answer:** A
"""

    headers = {
        "Authorization": f"Bearer {API_KEY}",
        "Content-Type": "application/json"
    }

    payload = {
        "model": MODEL,
        "messages": [
            {"role": "system", "content": "You are a helpful assistant."},
            {"role": "user", "content": prompt}
        ],
        "temperature": 0.7,
        "max_tokens": 600
    }

    try:
        response = requests.post(API_URL, headers=headers, json=payload)
        if response.status_code == 200:
            return response.json()['choices'][0]['message']['content']
        else:
            print(f"Error {response.status_code}: {response.text}")
            return ""
    except Exception:
        traceback.print_exc()
        return ""

@app.route('/quiz/<topic>', methods=['GET'])
def get_quiz(topic):
    raw_text = fetch_quiz_from_model(topic)
    print("Received from model:\n", raw_text)

    pattern = r"\*\*Question:\*\*\s*(.*?)\s*A\.\s*(.*?)\s*B\.\s*(.*?)\s*C\.\s*(.*?)\s*D\.\s*(.*?)\s*\*\*Answer:\*\*\s*([A-D])"
    matches = re.findall(pattern, raw_text, re.DOTALL)

    quizzes = []
    for match in matches:
        question, a, b, c, d, correct_letter = match
        answers = [a, b, c, d]
        correct = answers[ord(correct_letter.upper()) - ord('A')]
        quizzes.append({
            "question": question.strip(),
            "answers": [a.strip(), b.strip(), c.strip(), d.strip()],
            "correct": correct.strip()
        })

    return jsonify({"quizList": quizzes})


@app.route('/get_tasks', methods=['GET'])
def get_tasks():
    interests_param = request.args.get('interests', '')
    selected_interests = [i.strip().lower() for i in interests_param.split(',') if i]

    all_tasks = [
    {"title": "Intro to Science", "description": "Test your knowledge on basic science topics."},
    {"title": "Tech Trends", "description": "Explore the latest in technology with this quiz."},
    {"title": "Engineering Basics", "description": "Quiz about core engineering principles."},
    {"title": "Mathematics Mastery", "description": "Sharpen your math skills."},
    {"title": "Creative Arts", "description": "Dive into the world of visual and performing arts."},
    {"title": "History of the World", "description": "Test your knowledge on world history events."},
    {"title": "Geography Exploration", "description": "Discover the wonders of the world through geography."},
    {"title": "Computer Science Fundamentals", "description": "Learn the basics of computer science and programming."},
    {"title": "Health & Wellness", "description": "A quiz on healthy living and well-being practices."},
    {"title": "Literature Classics", "description": "Explore the world of classic literature and famous authors."},
    {"title": "Environmental Science", "description": "Learn about the science of the environment and conservation."},
    {"title": "Economics 101", "description": "Understand the basics of economics and its key concepts."},
    {"title": "Psychology Basics", "description": "A quiz on the fundamentals of psychology and human behavior."},
    {"title": "Philosophy Thought", "description": "Test your knowledge on key philosophical concepts and thinkers."},
    {"title": "Music Theory", "description": "Explore the fundamentals of music theory and composition."},
    {"title": "AI & Machine Learning", "description": "A quiz about artificial intelligence and machine learning."},
    {"title": "Cybersecurity Essentials", "description": "Learn about the fundamentals of cybersecurity and protection."},
    {"title": "Robotics Engineering", "description": "Quiz on the principles and applications of robotics."},
    {"title": "Astronomy for Beginners", "description": "Explore the universe with this astronomy quiz."},
    {"title": "Biology Basics", "description": "Learn about the basics of biology and living organisms."},
    {"title": "Chemistry Foundations", "description": "A beginner's quiz on the science of chemistry."},
    {"title": "Physics and Forces", "description": "Test your knowledge on physics and fundamental forces."},
    {"title": "Modern Design", "description": "Learn about modern design principles and concepts."},
    {"title": "Media Studies", "description": "A quiz covering the history and impact of media on society."},
    {"title": "Languages of the World", "description": "Test your knowledge on languages spoken around the world."},
    {"title": "Programming Concepts", "description": "A beginner's quiz on key programming concepts and languages."},
    {"title": "Game Development Basics", "description": "Learn the basics of game design and development."},
    {"title": "Machine Learning Algorithms", "description": "Quiz on key algorithms and techniques in machine learning."},
    {"title": "Ethics and Society", "description": "Test your knowledge on ethical issues and societal impacts."},
    {"title": "Political Science Insights", "description": "A quiz on key political systems and theories."}
]


    # Filter tasks that match user interests
    filtered_tasks = [
        task for task in all_tasks
        if any(interest in task["title"].lower() or interest in task["description"].lower() for interest in selected_interests)
    ]

    return jsonify({"tasks": filtered_tasks})


@app.route('/generate_quiz', methods=['GET'])
def generate_quiz():
    topic = request.args.get('topic')
    if not topic:
        return jsonify({"error": "Topic parameter is required"}), 400

    raw_text = fetch_quiz_from_model(topic)
    print("Received from model:\n", raw_text)

    pattern = r"\*\*Question:\*\*\s*(.*?)\s*A\.\s*(.*?)\s*B\.\s*(.*?)\s*C\.\s*(.*?)\s*D\.\s*(.*?)\s*\*\*Answer:\*\*\s*([A-D])"
    matches = re.findall(pattern, raw_text, re.DOTALL)

    quizzes = []
    for match in matches:
        question, a, b, c, d, correct_letter = match
        answers = [a, b, c, d]
        correct = answers[ord(correct_letter.upper()) - ord('A')]
        quizzes.append({
            "question": question.strip(),
            "answers": [a.strip(), b.strip(), c.strip(), d.strip()],
            "correct": correct.strip()
        })

    return jsonify({"quizList": quizzes})


@app.route('/get_interests', methods=['GET'])
def get_interests():
    interests = [
        "Science", "Technology", "Engineering", "Mathematics", "Arts",
        "History", "Geography", "Computer Science", "Health", "Literature",
        "Environmental Science", "Economics", "Psychology", "Philosophy", "Music",
        "Artificial Intelligence", "Cybersecurity", "Robotics", "Astronomy", "Biology",
        "Chemistry", "Physics", "Design", "Media Studies", "Languages",
        "Programming", "Game Development", "Machine Learning", "Ethics", "Political Science"
    ]
    return jsonify({"interests": interests})

if __name__ == '__main__':
    port = 5001
    print(f"Server running at http://127.0.0.1:{port}")
    app.run(port=port, host="127.0.0.1")
