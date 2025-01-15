import { ChatDB } from "@/types";
import GroupChats from "../components/GroupChats";
import Messages from "../components/Messages";
import { useEffect, useState } from "react";

const Chat = () => {
  document.getElementById("root")!.style.padding = "0";
  document.getElementById("root")!.style.margin = "0";

  const [selectedChat, setSelectedChat] = useState<string | null>(null);
  const [chats, setChats] = useState<ChatDB[]>([]);

  useEffect(() => {
    const fetchChats = async () => {
      try {
        const response = await fetch(`/api/chatrooms/user/${1}`); // Get user ID from context
        if (!response.ok) {
          throw new Error("Failed to fetch chats");
        }
        const data = await response.json();
        setChats(data);
      } catch (err) {
        console.error("Error fetching chats from DB:", err);
      }
    }

    fetchChats();
  }, [selectedChat])

  return (
    <div className="flex w-screen h-screen">
      <GroupChats chats={chats} selectChat={setSelectedChat} setChats={setChats} />
      <Messages selectedChat={selectedChat} />
    </div>
  );
};

export default Chat;
