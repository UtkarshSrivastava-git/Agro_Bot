const functions = require("firebase-functions");
const { GoogleGenerativeAI } = require("@google/generative-ai");

const genAI = new GoogleGenerativeAI(process.env.GEMINI_API_KEY);

const SYSTEM_PROMPT = `You are KisanBot, an expert AI farming assistant for Indian farmers.
You help with:
- Crop selection based on season, region, and soil type
- Pest and disease identification and treatment
- Irrigation and water management
- Fertilizer and nutrient management
- Weather-based farming recommendations
- Post-harvest storage and market guidance
- Government schemes (PM-KISAN, Fasal Bima Yojana, etc.)

Rules:
- Keep answers concise, practical, and easy to understand
- Use simple language suitable for rural farmers
- If the user writes in Hindi or any regional language, reply in that same language
- Reply strictly in ${userLangTag}
- Always prioritize safety, low-cost solutions, and sustainability`;

const sessionHistories = {};

exports.api = functions.https.onRequest(async (req, res) => {

    res.set("Access-Control-Allow-Origin", "*");
    if (req.method === "OPTIONS") {
        res.set("Access-Control-Allow-Methods", "POST");
        res.set("Access-Control-Allow-Headers", "Content-Type");
        return res.status(204).send("");
    }

    if (req.method !== "POST") {
        return res.status(405).json({ error: "Method not allowed" });
    }

    if (req.path !== "/Chat") {
        return res.status(404).json({ error: "Route not found" });
    }

    const { message, lang, sessionId } = req.body;

    if (!message || typeof message !== "string" || message.trim() === "") {
        return res.status(400).json({ error: "message field is required" });
    }

    const session = sessionId || "default";

    try {
        const model = genAI.getGenerativeModel({
            model: "gemini-2.5-flash",
            systemInstruction: SYSTEM_PROMPT,
        });

        if (!sessionHistories[session]) {
            sessionHistories[session] = [];
        }

        const chat = model.startChat({
            history: sessionHistories[session],
        });

        const userMessage = lang && lang !== "en"
            ? `[User language: ${lang}]\n${message.trim()}`
            : message.trim();

        const result = await chat.sendMessage(userMessage);
        const reply = result.response.text();

        sessionHistories[session] = await chat.getHistory();

        if (sessionHistories[session].length > 20) {
            sessionHistories[session] = sessionHistories[session].slice(-20);
        }

        return res.status(200).json({ reply });

    } catch (error) {
        console.error("Gemini API error status:", error.status);
                console.error("Gemini API error message:", error.message);
                console.error("Gemini API error details:", JSON.stringify(error, null, 2));
                console.error("GEMINI_API_KEY present:", !!process.env.GEMINI_API_KEY);
                console.error("GEMINI_API_KEY length:", process.env.GEMINI_API_KEY?.length);
                if (error.status === 429) {
                    return res.status(429).json({ error: "Rate limit reached. Please try again shortly." });
                }
                return res.status(500).json({
                    error: "Failed to get response from AI.",
                    detail: error.message
                });
    }
});