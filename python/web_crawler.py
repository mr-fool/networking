import sys
import requests
import os
from bs4 import BeautifulSoup

def main():
	os.system('clear') # on linux / os x
	
	#Gathering necessary input from user
	choice = int( input("1 to use the basic function, 2 for the advance crawling, 3 for exit\n") )
	option(choice)
		
	base_url = input("Please enter the base url\n");
	max_pages = int( input("Please enter maximum amount of that you want to crawl thru\n") )
	
	#Debug process 
	#print("base url",base_url)
	#print("max pages", max_page)
	
def spider(base_url,max_pages):
	page = 1
	while page <= max_pages:
		url = base_url + str(page)
		#storing all the information including headers in the variable source code
		source_code = requests.get(url)
		
		#sort source code and store only the plaintext
		plain_text = source_code.text
		
		#converting plain_text to Beautiful Soup object so the library can sort thru it
		convert_data = BeautifulSoup(plain_text)
		
		#sorting useful information
		for link in convert_data.findAll('a', {'class': 'item-name'}):

			href = base_url + link.get('href') #Building a clickable url
			title = link.string #just the text not the html
			#displaying the result back to the user
			print(href)
			print(title)
		page = page + 1

def option(choice):
	if choice == 1 :
		spider(base_url,max_pages)
	elif choice == 3:
		sys.exit() 

main()
