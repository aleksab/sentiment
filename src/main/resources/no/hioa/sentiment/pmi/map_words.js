function printDistance() {	
	var words = this.content.replace(/\./g,'').replace(/\,/g,'').split(' ');
	emit(1, words.length - 1);	
}