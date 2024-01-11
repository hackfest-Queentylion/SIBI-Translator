package main

import (
	"fmt"
	"github.com/gin-gonic/gin"
	"github.com/gorilla/websocket"
	"net/http"
)

var wsupgrader = websocket.Upgrader{
	ReadBufferSize:  1024,
	WriteBufferSize: 1024,
	CheckOrigin: func(r *http.Request) bool {
		return true // Allow all origins, adjust in production
	},
}

func main() {
	r := gin.Default()

	r.GET("/", func(c *gin.Context) {
		c.JSON(200, gin.H{
			"message": "Hello world!",
		})
	})

	r.GET("/ws", func(c *gin.Context) {
		ws, err := wsupgrader.Upgrade(c.Writer, c.Request, nil)
		if err != nil {
			return
		}
		defer func(ws *websocket.Conn) {
			err := ws.Close()
			if err != nil {

			}
		}(ws)

		for {
			// Read
			_, msg, err := ws.ReadMessage()
			if err != nil {
				break
			}
			// Print the message to the console
			fmt.Printf("%s\n", msg)
			// Write
			err = ws.WriteMessage(websocket.TextMessage, msg)
			if err != nil {
				break
			}
		}
	})

	err := r.Run()
	if err != nil {
		return
	}
}
