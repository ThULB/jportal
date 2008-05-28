window.onload=highlightCurrentPageLink;

function highlightCurrentPageLink() 
	{
	 var anzHeads = document.getElementsByTagName("th").length;
	 var checker = true; 
			
	 for (var i = 0; i <= anzHeads - 1; i++) 
	 	{
		 var anzHrefs = document.getElementsByTagName("th")[i].getElementsByTagName("a").length;
		 
		 for (var k = 0; k <= anzHrefs - 1; k++) 
	 		{
			 if (document.getElementsByTagName("th")[i].getElementsByTagName("a")[k].href == document.location.href) 
			 	{
			 	 document.getElementsByTagName("th")[i].getElementsByTagName("a")[k].setAttribute("id","navi_main_active");		 	
				 checker = false;
				}
			}	
		}
	//if sublink is in use parent link still should be highlighted, but its not working yet
	/* if(checker)
	 	{
	 	 var anzAs = document.getElementsByTagName("a").length;
	 	 var whilestopper = false;
	 	 var controltag;
	 	 var counter = 0;
	 	 for (var l = 0; l <= anzAs - 1; l++) 
	 	 	{
	 	 	 if (document.getElementsByTagName("a")[l].href == document.location.href) 
			 	{
			 	 controltag = document.getElementsByTagName("a")[l].parentNode.parentNode.parentNode.childNodes;
			 	 document.write(controltag);			 	 		 
				}
	 		}
	 	}	*/
	}